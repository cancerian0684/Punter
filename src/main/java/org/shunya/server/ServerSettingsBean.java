package org.shunya.server;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ServerSettingsBean {
    private int maxResultsToDisplay = 10;
    private int maxProcessHistory = 5;
    private int maxProcessAlerts = 30;
    private int webServerPort = 9991;
    private int maxWebServerThread = 5;
    private String tempDirectory;
    private String devEmail;

    public int getMaxResultsToDisplay() {
        return maxResultsToDisplay;
    }

    public void setMaxResultsToDisplay(int maxResultsToDisplay) {
        this.maxResultsToDisplay = maxResultsToDisplay;
    }

    public int getMaxProcessHistory() {
        return maxProcessHistory;
    }

    public void setMaxProcessHistory(int maxProcessHistory) {
        this.maxProcessHistory = maxProcessHistory;
    }

    public int getMaxProcessAlerts() {
        return maxProcessAlerts;
    }

    public void setMaxProcessAlerts(int maxProcessAlerts) {
        this.maxProcessAlerts = maxProcessAlerts;
    }

    public int getWebServerPort() {
        return webServerPort;
    }

    public void setWebServerPort(int webServerPort) {
        this.webServerPort = webServerPort;
    }

    public int getMaxWebServerThread() {
        return maxWebServerThread;
    }

    public void setMaxWebServerThread(int maxWebServerThread) {
        this.maxWebServerThread = maxWebServerThread;
    }

    public String getTempDirectory() {
        return tempDirectory;
    }

    public void setTempDirectory(String tempDirectory) {
        this.tempDirectory = tempDirectory;
    }

    public String getDevEmail() {
        return devEmail;
    }

    public void setDevEmail(String devEmail) {
        this.devEmail = devEmail;
    }
}
