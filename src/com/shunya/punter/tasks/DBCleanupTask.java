package com.shunya.punter.tasks;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;

import com.shunya.punter.annotations.InputParam;
import com.shunya.punter.annotations.PunterTask;
import com.shunya.punter.utils.StringUtils;

@PunterTask(author="munishc",name="DBCleanupTask",documentation= "docs/docs/DBCleanupTask.html")
public class DBCleanupTask extends Tasks {
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
			Class.forName("oracle.jdbc.driver.OracleDriver");
			if(conURL.indexOf("por")!=-1){
				throw new Exception("This task is not meant for Production DB's");
			}
			Connection conn = DriverManager.getConnection(conURL, username, password);
			LOGGER.get().log(Level.INFO, "Connected to DB");
			Statement s = conn.createStatement();
			s.setQueryTimeout(3*60);
//			s.executeUpdate(readFromFile("sql/test.sql","\n"));
			ResultSet rs=s.executeQuery(readFromFile("sql/drop.sql"," "));
			int i=0;
			while(rs.next()){
				i++;
				String sql=rs.getString(1);
				LOGGER.get().log(Level.INFO, ""+i+" "+sql);
				Statement dropStatement = conn.createStatement();
				try{
				dropStatement.executeUpdate(sql);
				dropStatement.close();
				}catch (Exception e) {
//					e.printStackTrace();
					LOGGER.get().log(Level.WARNING, ""+i+" "+sql+" Failed" +StringUtils.getExceptionStackTrace(e));
				}finally{
					dropStatement.close();
				}
			}
			LOGGER.get().log(Level.INFO, "DBCleanup Task completed successfully");
			/*CallableStatement cs=conn.prepareCall("{call MYPROC}");
			cs.setQueryTimeout(2*60);
			cs.executeUpdate();
			System.out.println("Database objects dropped successfully.");*/
			conn.close();
			LOGGER.get().log(Level.INFO, "Connection to DB Closed.");
			status=true;
		}catch (Exception e) {
			status=false;
			LOGGER.get().log(Level.SEVERE, StringUtils.getExceptionStackTrace(e));
		}
		 return status;
	}
	public static String readFromFile(String filename,String separator) {
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(DBCleanupTask.class.getClassLoader().getResourceAsStream(filename)));
			StringBuilder sb=new StringBuilder();
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line+separator);
			}
			return sb.toString();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (bufferedReader != null)
					bufferedReader.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}
}	