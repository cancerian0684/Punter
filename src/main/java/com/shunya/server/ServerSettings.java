package com.shunya.server;

import com.shunya.kb.jpa.Document;
import com.shunya.kb.jpa.SynonymWord;

import javax.jnlp.*;
import javax.management.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

public class ServerSettings implements ServerSettingsMBean, Serializable {
    private int maxResultsToDisplay = 10;
    private int maxProcessHistory = 5;
    private int maxProcessAlerts = 30;
    private int webServerPort = 8080;
    private int maxWebServerThread = 5;
    private String tempDirectory;

    private transient HibernateDaoFacade hibernateDaoFacade;

    public void setHibernateDaoFacade(HibernateDaoFacade hibernateDaoFacade) {
        this.hibernateDaoFacade = hibernateDaoFacade;
    }
    public ServerSettings() {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            mbs.registerMBean(this, new ObjectName("PunterServer:type=ServerSettings"));
            System.err.println("ServerSettings registered with MBean Server.");
        } catch (MBeanRegistrationException e) {
            e.printStackTrace();
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (InstanceAlreadyExistsException e) {
            e.printStackTrace();
        } catch (NotCompliantMBeanException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getMaxWebServerThread() {
        return maxWebServerThread < 1 ? 1 : maxWebServerThread;
    }

    @Override
    public void setMaxWebServerThread(int maxWebServerThread) {
        this.maxWebServerThread = maxWebServerThread;
    }

    @Override
    public String getDevEmailCSV() {
        return devEmailCSV == null ? "munish.chandel@ubs.com" : devEmailCSV;
    }

    @Override
    public void setDevEmailCSV(String devEmailCSV) {
        this.devEmailCSV = devEmailCSV;
    }

    private String devEmailCSV = "munish.chandel@ubs.com";

    private void saveState() {
        System.out.println("serializing the settings.");
        try {
            PersistenceService ps;
            BasicService bs;
            ps = (PersistenceService) ServiceManager.lookup("javax.jnlp.PersistenceService");
            bs = (BasicService) ServiceManager.lookup("javax.jnlp.BasicService");
            URL codebase = bs.getCodeBase();
            FileContents fc = ps.get(codebase);
            ObjectOutputStream oos = new ObjectOutputStream(fc.getOutputStream(true));
            oos.writeObject(this);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                FileOutputStream fout = new FileOutputStream("punter_server.dat");
                ObjectOutputStream oos = new ObjectOutputStream(fout);
                oos.writeObject(this);
                oos.close();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }

    @Override
    public void addSynonym(String words){
        SynonymWord synonymWord = new SynonymWord();
        synonymWord.setWords(words);
        hibernateDaoFacade.create(synonymWord);
        System.out.println("synonymWord = " + synonymWord.getWords());
    }

    @Override
    public void refreshIndexes() {
        System.err.println("Refreshing indexe's");
        hibernateDaoFacade.buildSynonymsCacheLocal();
        hibernateDaoFacade.rebuildIndex();
        System.err.println("Indexes refreshed");
    }

    @Override
    public void stopServer() {
        System.err.println("Stopping system.");
        System.exit(0);
    }

    @Override
    public void setMaxResultsToDisplay(int maxResults) {
        this.maxResultsToDisplay = maxResults;

    }

    @Override
    public int getMaxResultsToDisplay() {
        if (maxResultsToDisplay < 5)
            maxResultsToDisplay = 7;
        return maxResultsToDisplay;
    }

    @Override
    public int getWebServerPort() {
        if (webServerPort < 100)
            webServerPort = 8080;
        return webServerPort;
    }

    @Override
    public void setWebServerPort(int webServerPort) {
        this.webServerPort = webServerPort;
    }

    @Override
    public String getServerHostAddress() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }

    @Override
    public String getJNLPURL() throws UnknownHostException {
        return "http://" + getServerHostAddress() + ":" + getWebServerPort() + "/punter.jnlp";
    }

    @Override
    public int getMaxProcessHistory() {
        if (maxProcessHistory <= 5)
            maxProcessHistory = 5;
        return maxProcessHistory;
    }

    @Override
    public void setMaxProcessHistory(int maxProcessHistory) {
        this.maxProcessHistory = maxProcessHistory;
    }

    @Override
    public int getMaxProcessAlerts() {
        if (maxProcessAlerts <= 5)
            maxProcessAlerts = 30;
        return maxProcessAlerts;
    }

    @Override
    public void setMaxProcessAlerts(int maxProcessAlerts) {
        this.maxProcessAlerts = maxProcessAlerts;
    }

    private void loadState() {
        ServerSettingsMBean serverSettings;
        try {
            PersistenceService ps;
            BasicService bs;
            ps = (PersistenceService) ServiceManager
                    .lookup("javax.jnlp.PersistenceService");
            bs = (BasicService) ServiceManager
                    .lookup("javax.jnlp.BasicService");
            URL codebase = bs.getCodeBase();
            FileContents settings = null;
            settings = ps.get(codebase);
            ObjectInputStream ois = new ObjectInputStream(
                    settings.getInputStream());
            serverSettings = (ServerSettingsMBean) ois.readObject();
            ois.close();
        } catch (Exception fnfe) {
            try {
                PersistenceService ps;
                BasicService bs;
                ps = (PersistenceService) ServiceManager
                        .lookup("javax.jnlp.PersistenceService");
                bs = (BasicService) ServiceManager
                        .lookup("javax.jnlp.BasicService");
                URL codebase = bs.getCodeBase();
                long size = ps.create(codebase, 64000);
                System.out.println("Cache created - size: " + size);
            } catch (MalformedURLException murle) {
                System.err.println("Application codebase is not a valid URL?!");
                murle.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (UnavailableServiceException e) {
                e.printStackTrace();
                try {
                    FileInputStream fin = new FileInputStream(
                            "punter_server.dat");
                    ObjectInputStream ois = new ObjectInputStream(fin);
                    serverSettings = (ServerSettingsMBean) ois.readObject();
                    ois.close();
                    System.out.println("Settings loaded succesfully.");
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
            serverSettings=new ServerSettings();
        }
    }

    @Override
    public void updateAllProcessProperties() {
        hibernateDaoFacade.updateAllProcessProperties();
    }

    @Override
    public int deleteStaleProcessHistory(int staleDays) {
       return hibernateDaoFacade.deleteStaleHistory(staleDays);
    }

    @Override
    public void deleteDocument(int docId) {
        Document doc = new Document();
        doc.setId(docId);
        hibernateDaoFacade.deleteDocument(doc);
    }

    @Override
    public String toggleMultiCastResponder() {
        boolean state = MultiCastResponder.getInstance().toggle();
        if (state) {
            return "Server is running now";
        } else {
            return "Server is paused now";
        }
    }

    @Override
    public void compressTables() {
        hibernateDaoFacade.compressTables();
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
    public void restartAllClient() {
        SessionFacade.getInstance().sendMessageToAll("", new PunterRestartMessage());
    }
}