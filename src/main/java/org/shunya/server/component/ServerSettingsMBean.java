package org.shunya.server.component;

import java.net.UnknownHostException;

public interface ServerSettingsMBean {
    void addSynonym(String words);

    void refreshIndexes();

	void stopServer();

	void setMaxResultsToDisplay(int maxResults);

	int getMaxResultsToDisplay();

	void setMaxProcessHistory(int maxProcessHistory);

	int getMaxProcessHistory();

	void setMaxProcessAlerts(int maxProcessAlerts);

	int getMaxProcessAlerts();

	void updateAllProcessProperties();

	String toggleMultiCastResponder();

	int deleteStaleProcessHistory(int staleDays);

	void deleteDocument(int docId);

	void setWebServerPort(int webServerPort);

	int getWebServerPort();

    String getTempDirectory();

    void setTempDirectory(String tempDirectory);

    void saveState();

    String getDevEmailCSV();

    void setDevEmailCSV(String devEmailCSV);

    String getServerHostAddress() throws UnknownHostException;
}
