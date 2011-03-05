package com.sapient.punter.tasks;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.StringTokenizer;
import java.util.logging.Level;

import com.sapient.punter.annotations.InputParam;
import com.sapient.punter.annotations.PunterTask;

@PunterTask(author="munishc",name="CreateTablesTask",documentation="com/sapient/punter/tasks/docs/CreateTablesTask.html") 
public class CreateTablesTask extends Tasks {
	@InputParam(required = true,description="comma delimited sql DDL script") 
	private String SQLScript;
	@InputParam(required = true,description="jdbc:oracle:thin:@xldn2738dor.ldn.swissbank.com:1523:DELSHRD1") 
	private String conURL;
	@InputParam(required = false,description="DAISY4")
	private String username;
	@InputParam(required = false,description="Welcome1")
	private String password;
	
	@Override
	public boolean run() {
		boolean status=false;
		try{
//			String existingSQL=readFromFile("sql/daisyCreate.sql","\n");
//			System.out.println(getCreateTableSql(existingSQL));
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection conn = DriverManager.getConnection(conURL, username, password);
//			conn.setReadOnly(true);
			try{
//			System.out.println("Connected to DB");
			StringTokenizer sk=new StringTokenizer(SQLScript, ";");
			while(sk.hasMoreTokens()){
				String currSql=sk.nextToken().trim();
				if(!currSql.isEmpty()){
				LOGGER.get().log(Level.INFO, currSql);
				Statement s = conn.createStatement();
				boolean gotResult = s.execute(currSql);
				do {
				      SQLWarning warning = s.getWarnings();
				      if (gotResult) {
//				        if (delegate != null) {
//				          delegate.processResult(s.getResultSet());
//				        }
				      }
				      else
				      LOGGER.get().log(Level.INFO, "rows updated : "+s.getUpdateCount());
				    }
				    while (s.getMoreResults());
				s.close();
				}
			}
			/*CallableStatement call = conn.prepareCall("{"+existingSQL+"}");
			call.execute();*/
			LOGGER.get().log(Level.INFO, "All Tables created successfully.");
			}finally{
				conn.close();
			}

			status=true;
		}catch (Exception e) {
			status=false;
			LOGGER.get().log(Level.SEVERE, e.getMessage());
		}
		 return status;
	}
}	