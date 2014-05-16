package com.shunya.punter.tasks;

import com.shunya.punter.annotations.InputParam;
import com.shunya.punter.annotations.OutputParam;
import com.shunya.punter.annotations.PunterTask;
import com.shunya.punter.utils.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@PunterTask(author="munishc",name="CopyTableDataTask",description="Copie's Select Queries resultset to target Table.",documentation= "src/main/resources/docs/CopyTableDataTask.html")
public class CopyTableDataTask extends Tasks {
	@InputParam(required = true,description="Enter source DB URL Connection.")
	private String sourceDbURL;
	@InputParam(required = true)
	private String sourceDbUsername;
	@InputParam(required = true)
	private String sourceDbPassword;
	@InputParam(required = true)
	private String dataQuery;
	@InputParam(required = true)
	private String targetDbURL;
	@InputParam(required = true)
	private String targetDbUsername;
	@InputParam(required = true)
	private String targetDbPassword;
	@InputParam(required = true)
	private String preparedStatementForInsert;
	@InputParam(required = false)
	private int fetchSize;
    @InputParam(required = false)
    private String driverClass;

    @OutputParam
	private String rowsUpdated;

	@Override
	public boolean run() {
		boolean status=false;
		try{
			if(fetchSize<10)
				fetchSize=10;
            if(driverClass==null || driverClass.isEmpty()){
                driverClass="oracle.jdbc.driver.OracleDriver";
            }
			Class.forName(driverClass);
			Connection sourceCon = DriverManager.getConnection(sourceDbURL, sourceDbUsername, sourceDbPassword);
			Connection targetCon = DriverManager.getConnection(targetDbURL, targetDbUsername, targetDbPassword);
			sourceCon.setReadOnly(true);
			targetCon.setAutoCommit(false);
			Statement stmt=sourceCon.createStatement();
			PreparedStatement pstmt = targetCon.prepareStatement(preparedStatementForInsert);
			stmt.setFetchSize(fetchSize);
			ResultSet sourceRS = stmt.executeQuery(dataQuery);
			int rows=0;
			int columns=sourceRS.getMetaData().getColumnCount();
			List<Integer> columnDataType=new ArrayList<Integer>();
			for(int i=1;i<=columns;i++)
		  	 	{
		  	 		int dataType=sourceRS.getMetaData().getColumnType(i);
		  	 		columnDataType.add(dataType);
		  	 	}
			LOGGER.get().log(Level.INFO, "Inserting Data.");
			while(sourceRS.next()){
				try{
				for (int i=0; i<columns; i++) {
					Object obj=sourceRS.getObject(i+1);
					if(obj!=null){
						pstmt.setObject(i+1, obj);
					}else{
						pstmt.setNull(i+1,columnDataType.get(i));
					}
			    }
				pstmt.executeUpdate();
				rows++;
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			LOGGER.get().log(Level.INFO, "Commiting changes");
			targetCon.commit();
			sourceRS.close();
			stmt.close();
			pstmt.close();
			targetCon.close();
			sourceCon.close();
			LOGGER.get().log(Level.INFO, "Successfully Rows updated : "+rows);
			status=true;
		}catch (Exception e) {
            LOGGER.get().severe(StringUtils.getExceptionStackTrace(e));
		}
		return status;
	}
}