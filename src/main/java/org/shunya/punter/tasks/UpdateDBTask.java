package org.shunya.punter.tasks;

import org.shunya.punter.annotations.InputParam;
import org.shunya.punter.annotations.OutputParam;
import org.shunya.punter.utils.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Scanner;
import java.util.logging.Level;

/**
 * Created by munichan on 5/20/2014.
 */
public class UpdateDBTask extends Tasks {
    @InputParam(required = true, description = "jdbc:oracle:thin:@xldn2738dor.ldn.swissbank.com:1523:DELSHRD1")
    private String conURL;
    @InputParam(required = false, description = "DAISY2")
    private String username;
    @InputParam(required = false, description = "Welcome1")
    private String password;
    @InputParam(required = false, description = "sql strings")
    private String sql;
    @OutputParam
    private String output = "";

    @Override
    public boolean run() {
        boolean status = false;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            Connection conn = DriverManager.getConnection(conURL, username, password);
            conn.setReadOnly(false);
            LOGGER.get().log(Level.INFO, "Connected to DB");
            Scanner stk = new Scanner(sql).useDelimiter("\r\n|\n\r|\r|\n");
            while (stk.hasNext()) {
                String sqlLine = stk.next().trim();
                Statement s = conn.createStatement();
                {
                    s.setQueryTimeout(2 * 60);
                    output += "executing statement : " + sqlLine + "\n";
                    LOGGER.get().log(Level.INFO, "executing statement : " + sqlLine);
                    int count = s.executeUpdate(sqlLine);
                    s.close();
                    output += "records affected :" + count + "\n";
                    LOGGER.get().log(Level.INFO, "records affected :" + count);
                }
            }
            conn.close();
            LOGGER.get().log(Level.INFO, "Connection to DB Closed.");
            status = true;
        } catch (Exception ee) {
            LOGGER.get().log(Level.SEVERE, "Exception occurred "+ StringUtils.getExceptionStackTrace(ee));
        }
        return status;
    }
}
