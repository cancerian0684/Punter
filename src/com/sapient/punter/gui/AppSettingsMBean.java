package com.sapient.punter.gui;

import java.awt.Dimension;
import java.awt.Point;

public interface AppSettingsMBean {
	public abstract void setMaxResults(int maxResults);
	public abstract int getMaxResults();
	public abstract void setKBFrameDimension(Dimension kBFrameDimension);
	public abstract Dimension getKBFrameDimension();
	public abstract void setMultiSearchEnable(boolean multiSearchEnable);
	public abstract boolean isMultiSearchEnable();
	public abstract void setKeyStrokeFlush(int keystrokeFlush);
	public abstract int getKeyStrokeFlush();
	public abstract int getMaxKeyStrokeDelay();
	public abstract void setMaxKeyStrokeDelay(int maxKeyStrokeDelay);
	public abstract void setMaxExecutorSize(int maxExecutorSize);
	public abstract int getMaxExecutorSize();
	public abstract void setDocumentEditorLocation(Point documentEditorLocation);
	public abstract Point getDocumentEditorLocation();
	public abstract void setDocumentEditorLastDim(Dimension documentEditorLastDim);
	public abstract Dimension getDocumentEditorLastDim();
	public abstract void setNimbusLookNFeel(boolean isNimbusLookNFeel);
	public abstract boolean isNimbusLookNFeel();
	public abstract Point getTextAreaEditorLocation();
	public abstract void setTextAreaEditorLocation(Point textAreaEditorLocation);
	public abstract Dimension getTextAreaEditorLastDim();
	public abstract void setTextAreaEditorLastDim(Dimension textAreaEditorLastDim);
	public abstract void setSmtpPassword(String smtpPassword);
	public abstract void setSmtpUsername(String smtpUsername);
	public abstract void setSmtpHost(String smtpHost);
	public abstract String getSmtpUsername();
	public abstract String getSmtpPassword();
	public abstract String getSmtpHost();

    boolean isSchedulerRunning();

    void setSchedulerRunning(boolean schedulerRunning);

    String getUsername();

    void setUsername(String username);

    String getTempDirectory();

    void setTempDirectory(String tempDirectory);
}