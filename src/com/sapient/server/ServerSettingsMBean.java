package com.sapient.server;

public interface ServerSettingsMBean {
	public void refreshIndexes();

	public void stopServer();

	public void setMaxResultsToDisplay(int maxResults);

	public int getMaxResultsToDisplay();

	public void optimizeIndex();

	public abstract void setMaxProcessHistory(int maxProcessHistory);

	public abstract int getMaxProcessHistory();

	public abstract void setMaxProcessAlerts(int maxProcessAlerts);

	public abstract int getMaxProcessAlerts();

	public void updateAllProcessProperties();

	public abstract String toggleMultiCastResponder();

	public abstract void deleteStaleProcessHistory(int staleDays);

	public abstract void compressTables();

	public abstract void deleteDocument(int docId);

	public abstract void setWebServerPort(int webServerPort);

	public abstract int getWebServerPort();

    public String getTempDirectory();

    public void setTempDirectory(String tempDirectory);
}
