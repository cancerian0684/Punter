package com.sapient.server;

public interface ServerSettingsMBean {
public void refreshIndexes();
public void stopServer();
public void setMaxResultsToDisplay(int maxResults);
public int getMaxResultsToDisplay();
public void optimizeIndex();
}
