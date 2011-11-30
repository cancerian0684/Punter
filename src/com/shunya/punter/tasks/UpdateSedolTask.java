package com.shunya.punter.tasks;


import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import com.shunya.punter.annotations.InputParam;
import com.shunya.punter.annotations.PunterTask;

@PunterTask(author="sharmban",name="UpdateSedolTask",description="Updates Sedol for Fund in AISDB and DAISY",documentation= "docs/TextSamplerDemoHelp.html")
public class UpdateSedolTask extends Tasks{
	private Connection dAISyQADBConnection = null;
	@InputParam(required = true,description="jdbc:oracle:thin:@xldn2738dor.ldn.swissbank.com:1523:DELSHRD1")
	private String dAISyConnURL;
	@InputParam(required = true,description="DAISY3")
	private String dAISyUser;
	@InputParam(required = true,description="Welcome1")
    private String dAISyPswd;
    
    private Connection AISDBQADBConnection = null;
	@InputParam(required = true,description="jdbc:oracle:thin:@xldn2738dor.ldn.swissbank.com:1523:DELSHRD1")
	private String AISDBConnURL;
	@InputParam(required = true,description="AISDB3")
	private String AISDBUser;
	@InputParam(required = true,description="Welcome1")
    private String AISDBPswd;
	@InputParam(required = true,description="Fund Symbol")
	private String fundSymbol;
	@InputParam(required = true,description="New Sedol")
	private String newSedol;
	@InputParam(required = true,description="update Real Sedol as well ?")
	private boolean updateRealSedol;
	
	public Boolean updateSedol(String fundSymbol, String newSedol, Boolean updateRealSedol) throws Exception
	{
		Boolean result = false;
		String oldSedol = null;
		int num = 0;
		
		//for AISDB
		
		AISDBQADBConnection = ConnectionUtil.getConnection(AISDBConnURL, AISDBUser, AISDBPswd);
		LOGGER.get().log(Level.INFO, "Connected to AISDB");
		dAISyQADBConnection = ConnectionUtil.getConnection(dAISyConnURL, dAISyUser, dAISyPswd);
		LOGGER.get().log(Level.INFO, "Connected to DAISY");
		
		try {
			
			//check whether newSedol already exists
			//For AISDB
			PreparedStatement preparedStatement = AISDBQADBConnection.prepareStatement("select * from t_fund where tf_sedol = ?");
			preparedStatement.setString(1, newSedol);
			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next())
			{
				LOGGER.get().log(Level.INFO, "Sedol Already Exists in AISDB");
				throw new Exception("Sedol Already Exists in AISDB");
			}
			//For dAISy
			preparedStatement = dAISyQADBConnection.prepareStatement("select * from fund_feeder where sedol = ?");
			preparedStatement.setString(1, newSedol);
			resultSet = preparedStatement.executeQuery();
			if(resultSet.next())
			{
				LOGGER.get().log(Level.INFO, "Sedol Already Exists in DAISY");
				throw new Exception("Sedol Already Exists in dAISy");
			}
			
			//check whether symbol exists or not
			preparedStatement = AISDBQADBConnection.prepareStatement("select tf_sedol from t_fund where tf_symbol = ?");
			preparedStatement.setString(1, fundSymbol);
			resultSet = preparedStatement.executeQuery();
			if(!resultSet.next())
			{
				LOGGER.get().log(Level.INFO, "No such symbol exists in AISDB");
				throw new Exception("No such symbol exists in AISDB");
			}
			else
			{
			oldSedol = resultSet.getString("tf_sedol");
			}
			
			//code to execute procedure for updating sedol in AISDB
			CallableStatement callableStatement;
			
			callableStatement = AISDBQADBConnection.prepareCall("{call UPDATE_SEDOL(?,?,?)}");

			callableStatement.setString(1, oldSedol);
			callableStatement.setString(2, newSedol);
			callableStatement.registerOutParameter(3, java.sql.Types.VARCHAR);
			LOGGER.get().log(Level.INFO, "Calling Procedure UPDATE_SEDOL");
			num = callableStatement.executeUpdate();
			//callableStatement.execute();
			if(num!=1)
			{
				LOGGER.get().log(Level.INFO, "Can not update Sedol .. unknown reason");
				throw new Exception("Can not update Sedol .. unknown reason");
			}
			LOGGER.get().log(Level.INFO, "updated sedol in AISDB");
			num=0;
			//updating real sedol if required
			if(updateRealSedol == true)
			{
				preparedStatement = AISDBQADBConnection.prepareStatement("update t_fund set tf_real_sedol = ? where tf_sedol = ?");
				preparedStatement.setString(1, newSedol);
				preparedStatement.setString(2, newSedol);
				LOGGER.get().log(Level.INFO, "updating real sedol in AISDB");
				num = preparedStatement.executeUpdate();
				if(num == 0)
				{
					LOGGER.get().log(Level.INFO, "Can not update real Sedol in aisdb");
					throw new Exception("Can not update real Sedol");
				}
				num = 0; //because it is being used again below
			}
			
			
			//Checking if the symbol doesn't exist in dAISy, but is in AISDB ( when the fund has been setup on the same day as the updation of sedol has been asked for, then return
			
			
			preparedStatement = dAISyQADBConnection.prepareStatement("select * from fund_feeder where symbol = ?");
			preparedStatement.setString(1, fundSymbol);
			resultSet = preparedStatement.executeQuery();
			if(!resultSet.next())
			{
				LOGGER.get().log(Level.INFO, "This symbol does not exist in dAISy.");
				return true;
			}
			
			
			//Updating sedol in dAISy
			preparedStatement = dAISyQADBConnection.prepareStatement("update fund_feeder set sedol = ? where symbol = ?");
			preparedStatement.setString(1, newSedol);
			preparedStatement.setString(2, fundSymbol);
			num = preparedStatement.executeUpdate();

			if(num == 0)
			{
				LOGGER.get().log(Level.INFO, "Can not update Sedol in DAISY.");
				throw new Exception("Can not update Sedol in DAISY.");
			}
			LOGGER.get().log(Level.INFO, "Updated sedol in DAISY.");
			
			
			// all done, if reaches here
			
			result = true;
			
			
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}finally
		{//closing connections
			if(AISDBQADBConnection!=null)
			AISDBQADBConnection.close();
			if(dAISyQADBConnection!=null)
			dAISyQADBConnection.close();
			LOGGER.get().log(Level.INFO, "Closed all DB connections");
		}
		return result;
	}

	@Override
	public boolean run() {
		Boolean result = false;
		try{
			result = updateSedol(fundSymbol,newSedol,updateRealSedol);
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			LOGGER.get().log(Level.WARNING,exception.getMessage());
		}
		finally
		{
			if(result == false)
			{
				LOGGER.get().log(Level.WARNING, "Sedol can not be updated");
			}
			else
			{
				LOGGER.get().log(Level.INFO, "Sedol updated :) ");
			}
		}
		return result;
	}
}
