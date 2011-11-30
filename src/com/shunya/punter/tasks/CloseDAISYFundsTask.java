package com.shunya.punter.tasks;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Scanner;
import java.util.logging.Level;

import com.shunya.punter.annotations.InputParam;
import com.shunya.punter.annotations.PunterTask;
import com.shunya.punter.utils.StringUtils;

@PunterTask(author="munishc",name="CloseDAISYFundsTask",description="Close Out DAISY Funds.",documentation= "docs/TextSamplerDemoHelp.html")
public class CloseDAISYFundsTask extends Tasks {
	@InputParam(required = true,description="jdbc:oracle:thin:@xldn2738dor.ldn.swissbank.com:1523:DELSHRD1") 
	private String conURL;
	@InputParam(required = false,description="DAISY2")
	private String username;
	@InputParam(required = false,description="Welcome1")
	private String password;

	@InputParam(required = false,description="Line separated FundSymbols")
	private String fundSymbols;

	@Override
	public boolean run() {
		boolean status=false;
		try{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection conn = DriverManager.getConnection(conURL, username, password);
			LOGGER.get().log(Level.INFO, "Connected to DB");
			Scanner stk = new Scanner(fundSymbols).useDelimiter("\r\n|\n\r|\r|\n|,| |;");
			int counter=0;
		      while (stk.hasNext()) {
				String fundSymbol = stk.next().trim();
				if(fundSymbol!=null && !fundSymbol.isEmpty()){
				Statement s = conn.createStatement();
				boolean gotResult = s.execute("update fund_feeder set state = 'CLOSED' where symbol ='"+fundSymbol+"'");
				counter++;
				do {
				      SQLWarning warning = s.getWarnings();
				      if (gotResult) {
//					        if (delegate != null) {
//					          delegate.processResult(s.getResultSet());
//					        }
					      }
					  else
						  LOGGER.get().log(Level.INFO, counter+" - " + fundSymbol+" [rows updated : "+s.getUpdateCount()+ " ]["+warning+"]");
				    }
				    while (s.getMoreResults());
					s.close();
				}
		      }
		      conn.close();
		      LOGGER.get().log(Level.INFO, "Connection to DB Closed.");
			status=true;
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.get().log(Level.SEVERE, StringUtils.getExceptionStackTrace(e));
		}finally{
			
		}
		return status;
	}
}