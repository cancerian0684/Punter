package com.sapient.punter.tasks;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;

import com.sapient.punter.annotations.InputParam;
import com.sapient.punter.annotations.OutputParam;
import com.sapient.punter.annotations.PunterTask;
import com.sapient.punter.utils.StringUtils;

@PunterTask(author="munishc",name="GenTableDDLTask",documentation="com/sapient/punter/tasks/docs/GenTableDDLTask.html") 
public class GenTableDDLTask extends Tasks {
	@InputParam(required = true,description="jdbc:oracle:thin:@xldn2738dor.ldn.swissbank.com:1523:DELSHRD1") 
	private String conURL;
	@InputParam(required = false,description="DAISY2")
	private String username;
	@InputParam(required = false,description="Welcome1")
	private String password;
	
	@OutputParam
	private String tableDDLString;

	@Override
	public boolean run() {
		boolean status=false;
		try{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection conn = DriverManager.getConnection(conURL, username, password);
			conn.setReadOnly(true);
			LOGGER.get().log(Level.INFO, "Connected to DB");
			Statement s = conn.createStatement();
			s.setQueryTimeout(2*60);
			tableDDLString="";
//			s.executeUpdate(readFromFile("sql/test.sql","\n"));
			CallableStatement call = conn.prepareCall("{call DBMS_METADATA.SET_TRANSFORM_PARAM(DBMS_METADATA.SESSION_TRANSFORM,'STORAGE','FALSE')}");
			call.execute();
			call = conn.prepareCall("{call DBMS_METADATA.SET_TRANSFORM_PARAM(DBMS_METADATA.SESSION_TRANSFORM,'TABLESPACE','FALSE')}");
			call.execute();
			call = conn.prepareCall("{call DBMS_METADATA.SET_TRANSFORM_PARAM(DBMS_METADATA.SESSION_TRANSFORM,'SEGMENT_ATTRIBUTES','FALSE')}");
			call.execute();
			call = conn.prepareCall("{call DBMS_METADATA.SET_TRANSFORM_PARAM(DBMS_METADATA.SESSION_TRANSFORM,'OID','TRUE')}");
			call.execute();
			call = conn.prepareCall("{call DBMS_METADATA.SET_TRANSFORM_PARAM(DBMS_METADATA.SESSION_TRANSFORM,'CONSTRAINTS','FALSE')}");
			call.execute();
			call = conn.prepareCall("{call DBMS_METADATA.SET_TRANSFORM_PARAM(DBMS_METADATA.SESSION_TRANSFORM,'REF_CONSTRAINTS','FALSE')}");
			call.execute();
			call = conn.prepareCall("{call DBMS_METADATA.SET_TRANSFORM_PARAM(DBMS_METADATA.SESSION_TRANSFORM,'SQLTERMINATOR','TRUE')}");
			call.execute();
			ResultSet rs=s.executeQuery("select table_name from user_tables");
			while(rs.next()){
				String tableName=rs.getString("table_name");
				Statement s1=conn.createStatement();
				ResultSet rs1 = s1.executeQuery("select DBMS_METADATA.GET_DDL('TABLE','"+tableName+"') from user_tables");
				rs1.next();
				String tableDDL=getCreateTableSql(rs1.getString(1)).replace('"', ' ')+";";
				tableDDLString=tableDDLString+tableDDL;
				LOGGER.get().log(Level.INFO, tableDDL);
				rs1.close();
			}
			s.close();
			conn.close();
			LOGGER.get().log(Level.INFO, "Connection to DB Closed.");
			status=true;
		}catch (Exception e) {
			status=false;
			LOGGER.get().log(Level.SEVERE, StringUtils.getExceptionStackTrace(e));
		}
		 return status;
	}
	public static String getCreateTableSql(String existingSQL){
		StringBuilder resultedSQL=new StringBuilder();
		int len1=existingSQL.indexOf('"');
		resultedSQL.append(existingSQL.substring(0, len1));
		resultedSQL.append(existingSQL.substring(existingSQL.indexOf('.')+1, existingSQL.indexOf('(')));
		resultedSQL.append(returnBraketData(existingSQL.substring(existingSQL.indexOf('(')-1)));
		return resultedSQL.toString();
	}
	public static String returnBraketData(String in){
		int j=0;
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<in.length();i++){
			switch(in.charAt(i)){
			case '(':
				j++;
				sb.append(in.charAt(i));
				break;
			case ')':
				j--;
				sb.append(in.charAt(i));
				if(j==0){
					return sb.toString();
				}
				break;
			default:
				sb.append(in.charAt(i));
			}
			
		}
		return "";
	}
	public static String readFromFile(String filename,String separator) {
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(filename));
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