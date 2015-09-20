package org.shunya.punter.gui;

import org.shunya.kb.utils.Utilities;

import javax.management.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement
public class AppSettings implements Serializable, AppSettingsMBean {
    private static AppSettings appSettings;

    private PunterPoint KBFrameLocationPoint = new PunterPoint(0, 0);
    private PunterPoint PunterGuiLocationPoint = new PunterPoint(0, 0);
    private PunterPoint TextAreaEditorLocationPoint = new PunterPoint(0, 0);
    private PunterPoint DocumentEditorLocation = new PunterPoint(0, 0);
    private PunterDimension KBFrameDimension;
    private PunterDimension DocumentEditorLastDim = new PunterDimension(800, 800);;
    private PunterDimension TextAreaEditorLastDim = new PunterDimension(400, 400);
    private int maxResults = 50;
    private boolean isNimbusLookNFeel = true;
    private boolean multiSearchEnable = true;
    private String username;
    private int keystrokeFlush = 3;
    private int maxKeyStrokeDelay = 200;
    private int maxExecutorSize = 2;
    private String smtpHost;
    private String smtpUsername;
    private String smtpPassword;
    private Map<String, Object> cache = new HashMap<>();
    private Map<String, String> sessionMap = new HashMap<>();
    private boolean schedulerEnabled = false;
    private String tempDirectory = "Temp";
    private boolean showActiveTasks = false;
    private int editorPreviewSize = 12;
    private int editorEditSize = 12;

    @Override
    public String getServerHost() {
        return serverHost;
    }

    @Override
    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    private String serverHost;

    public AppSettings() {
    }

    public Map<String, String> getSessionMap() {
        return sessionMap ;
    }

    public Map<String, Object> getCache() {
        return cache;
    }

    public void setCache(Map<String, Object> cache) {
        this.cache = cache;
    }

    public void setSessionMap(Map<String, String> sessionMap) {
        this.sessionMap = sessionMap;
    }

    public Object getObject(String key) {
        return cache.get(key);
    }

    public void setObject(String key, Object object) {
        cache.put(key, object);
    }

    @Override
    public String getSmtpHost() {
        return smtpHost;
    }

    @Override
    public String getSmtpPassword() {
        return smtpPassword;
    }

    @Override
    public String getSmtpUsername() {
        return smtpUsername;
    }

    @Override
    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    @Override
    public void setSmtpUsername(String smtpUsername) {
        this.smtpUsername = smtpUsername;
    }

    @Override
    public void setSmtpPassword(String smtpPassword) {
        this.smtpPassword = smtpPassword;
    }

    @Override
    public boolean isNimbusLookNFeel() {
        return isNimbusLookNFeel;
    }

    @Override
    public void setNimbusLookNFeel(boolean isNimbusLookNFeel) {
        this.isNimbusLookNFeel = isNimbusLookNFeel;
    }

    @Override
    public PunterDimension getDocumentEditorLastDim() {
        return DocumentEditorLastDim;
    }

    @Override
    public void setDocumentEditorLastDim(PunterDimension documentEditorLastDim) {
        DocumentEditorLastDim = documentEditorLastDim;
    }

    @Override
    public void setTextAreaEditorLastDim(PunterDimension textAreaEditorLastDim) {
        TextAreaEditorLastDim = textAreaEditorLastDim;
    }

    @Override
    public PunterDimension getTextAreaEditorLastDim() {
        return TextAreaEditorLastDim;
    }

    @Override
    public PunterPoint getDocumentEditorLocation() {
        return DocumentEditorLocation;
    }

    @Override
    public void setDocumentEditorLocation(PunterPoint documentEditorLocation) {
        DocumentEditorLocation = documentEditorLocation;
    }

    @Override
    public int getMaxExecutorSize() {
        return maxExecutorSize;
    }

    @Override
    public void setMaxExecutorSize(int maxExecutorSize) {
        this.maxExecutorSize = maxExecutorSize;
    }

    @Override
    public void setMaxKeyStrokeDelay(int maxKeyStrokeDelay) {
        this.maxKeyStrokeDelay = maxKeyStrokeDelay;
    }

    @Override
    public int getMaxKeyStrokeDelay() {
        return maxKeyStrokeDelay;
    }

    @Override
    public int getKeyStrokeFlush() {
        return keystrokeFlush;
    }

    @Override
    public void setKeyStrokeFlush(int keystrokeFlush) {
        this.keystrokeFlush = keystrokeFlush;
    }

    @Override
    public boolean isMultiSearchEnable() {
        return multiSearchEnable;
    }

    @Override
    public void setMultiSearchEnable(boolean multiSearchEnable) {
        this.multiSearchEnable = multiSearchEnable;
    }

    @Override
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    @Override
    public int getMaxResults() {
        return maxResults;
    }

    @Override
    public PunterDimension getKBFrameDimension() {
        return KBFrameDimension;
    }

    public void setKBFrameDimension(PunterDimension KBFrameDimension) {
        this.KBFrameDimension = KBFrameDimension;
    }

    public static AppSettings getInstance() {
        if (appSettings == null) {
            loadState();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    super.run();
                    saveState();
                }
            });
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            try {
                mbs.registerMBean(appSettings, new ObjectName("PunterClient:type=ClientSettings"));
                System.err.println("AppSettings registered with MBean Server.");
            } catch (MBeanRegistrationException | NotCompliantMBeanException | InstanceAlreadyExistsException | NullPointerException | MalformedObjectNameException e) {
                e.printStackTrace();
            }
        }
        return appSettings;
    }

    public static void saveState() {
        AppSettings appSettings = AppSettings.getInstance();
        try {
            Utilities.save(appSettings, "punter-app-settings.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadState() {
        try {
            appSettings = Utilities.loadJson(AppSettings.class, "punter-app-settings.json");
        } catch (Exception e) {
            appSettings = new AppSettings();
            e.printStackTrace();
        }
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean isSchedulerEnabled() {
        return schedulerEnabled;
    }

    @Override
    public void setSchedulerEnabled(boolean schedulerEnabled) {
        this.schedulerEnabled = schedulerEnabled;
    }

    @Override
    public String getTempDirectory() {
        return tempDirectory == null ? "Temp" : tempDirectory;
    }

    @Override
    public void setTempDirectory(String tempDirectory) {
        this.tempDirectory = tempDirectory;
    }

    @Override
    public boolean isShowActiveTasks() {
        return showActiveTasks;
    }

    @Override
    public void setShowActiveTasks(boolean showActiveTasks) {
        this.showActiveTasks = showActiveTasks;
    }

    public int getEditorPreviewSize() {
        return editorPreviewSize;
    }

    public void setEditorPreviewSize(int editorPreviewSize) {
        this.editorPreviewSize = editorPreviewSize;
    }

    public int getEditorEditSize() {
        return editorEditSize;
    }

    public void setEditorEditSize(int editorEditSize) {
        this.editorEditSize = editorEditSize;
    }

    public PunterPoint getKBFrameLocationPoint() {
        return KBFrameLocationPoint;
    }

    public void setKBFrameLocationPoint(PunterPoint KBFrameLocationPoint) {
        this.KBFrameLocationPoint = KBFrameLocationPoint;
    }

    public PunterPoint getPunterGuiLocationPoint() {
        return PunterGuiLocationPoint;
    }

    public void setPunterGuiLocationPoint(PunterPoint punterGuiLocationPoint) {
        this.PunterGuiLocationPoint = punterGuiLocationPoint;
    }

    @Override
    public PunterPoint getTextAreaEditorLocationPoint() {
        return TextAreaEditorLocationPoint;
    }

    @Override
    public void setTextAreaEditorLocationPoint(PunterPoint textAreaEditorLocationPoint) {
        TextAreaEditorLocationPoint = textAreaEditorLocationPoint;
    }
}
