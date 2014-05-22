package org.shunya.server.component;

import org.shunya.kb.model.Document;
import org.shunya.kb.model.SynonymWord;
import org.shunya.kb.utils.Utilities;
import org.shunya.server.MultiCastResponder;
import org.shunya.server.ServerSettingsBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Service
public class ServerSettings implements ServerSettingsMBean, Serializable {
    private ServerSettingsBean settingsBean;

    @Autowired
    private StaticDaoFacade daoFacade;

    @PostConstruct
    public void registerMbean() throws InstantiationException, IllegalAccessException {
        settingsBean = Utilities.load(ServerSettingsBean.class, "punter-server-settings.xml");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            mbs.registerMBean(this, new ObjectName("PunterServer:type=ServerSettings"));
            System.out.println("ServerSettings registered with MBean Server.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    @Override
    public void saveState(){
        System.out.println("Saving Server Settings");
        Utilities.save(ServerSettingsBean.class, settingsBean, "punter-server-settings.xml");
    }

    @Override
    public String getDevEmailCSV() {
        return settingsBean.getDevEmail() == null ? "cancerian0684@gmail.com" : settingsBean.getDevEmail();
    }

    @Override
    public void setDevEmailCSV(String devEmailCSV) {
        this.settingsBean.setDevEmail(devEmailCSV);
    }

    @Override
    public void addSynonym(String words) {
        SynonymWord synonymWord = new SynonymWord();
        synonymWord.setWords(words);
        daoFacade.create(synonymWord);
        System.out.println("synonymWord = " + synonymWord.getWords());
    }

    @Override
    public void refreshIndexes() {
        daoFacade.refreshIndexes();
    }

    @Override
    public void stopServer() {
        System.err.println("Stopping system.");
        System.exit(0);
    }

    @Override
    public void setMaxResultsToDisplay(int maxResults) {
        this.settingsBean.setMaxResultsToDisplay(maxResults);

    }

    @Override
    public int getMaxResultsToDisplay() {
        return Math.min(10, settingsBean.getMaxResultsToDisplay());
    }

    @Override
    public int getWebServerPort() {
        return settingsBean.getWebServerPort();
    }

    @Override
    public void setWebServerPort(int webServerPort) {
        this.settingsBean.setWebServerPort(webServerPort);
    }

    @Override
    public String getServerHostAddress() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }

    @Override
    public int getMaxProcessHistory() {
        return Math.min(5, settingsBean.getMaxProcessHistory());
    }

    @Override
    public void setMaxProcessHistory(int maxProcessHistory) {
        this.settingsBean.setMaxProcessHistory(maxProcessHistory);
    }

    @Override
    public int getMaxProcessAlerts() {
        return Math.min(30, settingsBean.getMaxProcessAlerts());
    }

    @Override
    public void setMaxProcessAlerts(int maxProcessAlerts) {
        this.settingsBean.setMaxProcessAlerts(maxProcessAlerts);
    }

    @Override
    public void updateAllProcessProperties() {
        daoFacade.updateAllProcessProperties();
    }

    @Override
    public int deleteStaleProcessHistory(int staleDays) {
        return daoFacade.deleteStaleHistory(staleDays);
    }

    @Override
    public void deleteDocument(int docId) {
        Document doc = new Document();
        doc.setId(docId);
        daoFacade.deleteDocument(doc);
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
        daoFacade.compressTables();
    }

    @Override
    public String getTempDirectory() {
        return settingsBean.getTempDirectory() == null ? "Temp" : settingsBean.getTempDirectory();
    }

    @Override
    public void setTempDirectory(String tempDirectory) {
        this.settingsBean.setTempDirectory(tempDirectory);
    }

}