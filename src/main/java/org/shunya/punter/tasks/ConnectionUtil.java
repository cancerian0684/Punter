package org.shunya.punter.tasks;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionUtil {
	private static Connection connection = null;
		
	public ConnectionUtil()
	{}
	
	public static Connection getConnection(String connectionURL, String username, String password)
	{
		try{
			// Load the JDBC driver
			String driverName = "oracle.jdbc.driver.OracleDriver";
			Class.forName(driverName);
			
			connection = DriverManager.getConnection(connectionURL, username, password);
	    }
		catch (ClassNotFoundException e) {
			// Could not find the database driver
		    e.printStackTrace();
			
		} catch (SQLException e) {
		    // Could not connect to the database
			e.printStackTrace();
		}
		
		return connection;
	}
}

