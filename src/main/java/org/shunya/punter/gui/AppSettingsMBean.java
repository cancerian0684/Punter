package org.shunya.punter.gui;

public interface AppSettingsMBean {
	void setMaxResults(int maxResults);
	int getMaxResults();
	void setKBFrameDimension(PunterDimension kBFrameDimension);
	PunterDimension getKBFrameDimension();
	void setMultiSearchEnable(boolean multiSearchEnable);
	boolean isMultiSearchEnable();
	void setKeyStrokeFlush(int keystrokeFlush);
	int getKeyStrokeFlush();
	int getMaxKeyStrokeDelay();
	void setMaxKeyStrokeDelay(int maxKeyStrokeDelay);
	void setMaxExecutorSize(int maxExecutorSize);
	int getMaxExecutorSize();
	void setDocumentEditorLocation(PunterPoint documentEditorLocation);
	PunterPoint getDocumentEditorLocation();
	void setDocumentEditorLastDim(PunterDimension documentEditorLastDim);
	PunterDimension getDocumentEditorLastDim();
	void setNimbusLookNFeel(boolean isNimbusLookNFeel);
	boolean isNimbusLookNFeel();
	PunterPoint getTextAreaEditorLocationPoint();
	void setTextAreaEditorLocationPoint(PunterPoint textAreaEditorLocation);
	PunterDimension getTextAreaEditorLastDim();
	void setTextAreaEditorLastDim(PunterDimension textAreaEditorLastDim);
	void setSmtpPassword(String smtpPassword);
	void setSmtpUsername(String smtpUsername);
	void setSmtpHost(String smtpHost);
	String getSmtpUsername();
	String getSmtpPassword();
	String getSmtpHost();

    boolean isSchedulerEnabled();

    void setSchedulerEnabled(boolean schedulerRunning);

    String getUsername();

    void setUsername(String username);

    String getTempDirectory();

    void setTempDirectory(String tempDirectory);

    boolean isShowActiveTasks();

    void setShowActiveTasks(boolean showActiveTasks);

    String getServerHost();

    void setServerHost(String serverHost);
}