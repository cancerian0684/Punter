package org.shunya.punter.tasks;

import org.shunya.punter.annotations.InputParam;
import org.shunya.punter.annotations.PunterTask;
import org.shunya.punter.utils.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.StringTokenizer;

@PunterTask(author = "munishc", name = "CreateTablesTask", documentation = "src/main/resources/docs/CreateTablesTask.html")
public class CreateTablesTask extends Tasks {
	@InputParam(required = true, description = "comma delimited sql DDL script")
	private String SQLScript;
	@InputParam(required = true, description = "jdbc:oracle:thin:@hostname:1523:DELSHRD1")
	private String conURL;
	@InputParam(required = false, description = "DAISY4")
	private String username;
	@InputParam(required = false, description = "Welcome1")
	private String password;

	@Override
	public boolean run() {
		boolean status = false;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection conn = DriverManager.getConnection(conURL, username, password);
			try {
				LOGGER.get().info("Connected to DB");
				StringTokenizer sk = new StringTokenizer(SQLScript, ";");
				while (sk.hasMoreTokens()) {
					String currSql = sk.nextToken().trim();
					if (!currSql.isEmpty()) {
						LOGGER.get().info(currSql);
						Statement s = conn.createStatement();
						boolean gotResult = s.execute(currSql);
						do {
							SQLWarning warning = s.getWarnings();
							if (gotResult) {
								// if (delegate != null) {
								// delegate.processResult(s.getResultSet());
								// }
							} else
								LOGGER.get().info("rows updated : " + s.getUpdateCount());
						} while (s.getMoreResults());
						s.close();
					}
				}
				/*
				 * CallableStatement call =
				 * conn.prepareCall("{"+existingSQL+"}"); call.execute();
				 */
				LOGGER.get().info(taskDao.getDescription() + " Completed.");
			} finally {
				conn.close();
			}
			status = true;
		} catch (Exception e) {
			status = false;
			LOGGER.get().error(StringUtils.getExceptionStackTrace(e));
		}
		return status;
	}
}