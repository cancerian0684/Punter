package com.sapient.punter.tasks;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;

import com.sapient.punter.annotations.InputParam;
import com.sapient.punter.annotations.OutputParam;
import com.sapient.punter.annotations.PunterTask;
import com.sapient.punter.utils.StringUtils;

@PunterTask(author="munishc",name="GenTableDDLTask",documentation="com/sapient/punter/tasks/docs/GenTableDDLTask.html") 
public class SelectQueryTask extends Tasks {
	@InputParam(required = true,description="jdbc:oracle:thin:@xldn2738dor.ldn.swissbank.com:1523:DELSHRD1") 
	private String conURL;
	@InputParam(required = true,description="DAISY2")
	private String username;
	@InputParam(required = true,description="Welcome1")
	private String password;
	@InputParam(required = true,description="select * from dual")
	private String query;
	
	@OutputParam
	private String queryOutput;
	public static void main(String[] args) {
		SelectQueryTask sqt=new SelectQueryTask();
		sqt.run();
	}
	@Override
	public boolean run() {
		boolean status=false;
		try{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection conn = DriverManager.getConnection(conURL, username, password);
//			Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@xldn2738dor.ldn.swissbank.com:1523:DELSHRD1", "AISDB4", "Welcome1");
			conn.setReadOnly(true);
			Scanner stk = new Scanner(query).useDelimiter("\r\n|\n\r|\r|\n");
		    while (stk.hasNext()) {
				String token = stk.next().trim();
				if(!token.isEmpty()){
					Statement s = conn.createStatement();
					s.setQueryTimeout(2*60);
					ResultSet rs=s.executeQuery(token);
					ResultSetMetaData metaData = rs.getMetaData();
					int columns=metaData.getColumnCount();
					List<String> columnNames=new ArrayList<String>();
					String out="";
					for (int i = 1; i <= columns; i++) {
						columnNames.add(metaData.getColumnName(i));
						out+=metaData.getColumnName(i)+"\t";
					}
//					System.out.println(out);
					LOGGER.get().log(Level.INFO, out);
					rs.setFetchSize(20);
					while(rs.next()){
						out="";
						for (int i = 1; i <= columns; i++) {
							out+=rs.getString(i)+"\t";
						}
						LOGGER.get().log(Level.INFO, out);
					}
					s.close();
					LOGGER.get().log(Level.INFO, "\n-------------------------************-------------------------\n");
				}
		    }
			conn.close();
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