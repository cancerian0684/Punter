package com.shunya.server.component;

import com.shunya.kb.gui.SearchQuery;
import com.shunya.kb.jpa.Attachment;
import com.shunya.kb.jpa.Document;
import com.shunya.punter.gui.AppSettings;
import com.shunya.punter.gui.PunterJobBasket;
import com.shunya.punter.gui.SingleInstanceFileLock;
import com.shunya.punter.jpa.ProcessData;
import com.shunya.punter.jpa.ProcessHistory;
import com.shunya.punter.jpa.TaskData;
import com.shunya.punter.jpa.TaskHistory;
import com.shunya.punter.utils.ClipBoardListener;
import com.shunya.server.*;
import com.shunya.server.model.JPASessionFactory;
import com.shunya.server.model.JPATransatomatic;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;

@Service
public class StaticDaoFacadeLocal implements StaticDaoFacade {
    private PunterSearch stub;
    private ClipBoardListener clipBoardListener;
    private SingleInstanceFileLock singleInstanceFileLock;
    private com.shunya.server.StaticDaoFacade staticDaoFacade;
    private SessionFacade sessionFacade;
    private JPATransatomatic transatomatic;
    private PunterHttpServer punterHttpServer;
    private ServerSettings serverSettings;
    private ServerContext context;
    private LocalJettyRunner jettyRunner;

    @Override
    public void setClipBoardListener(ClipBoardListener clipBoardListener) {
        this.clipBoardListener = clipBoardListener;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    private String sessionId;

    @Override
    public String getUsername() {
        return AppSettings.getInstance().getUsername();
    }

    @Override
    public void messageProcessor() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        PunterMessage message = getMessage();
                        process(message);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.setName("Message.Listener");
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    private void process(PunterMessage message) {
        if (message instanceof ClipboardPunterMessage && clipBoardListener !=null) {
            clipBoardListener.handleContent((ClipboardPunterMessage) message);
        } else if (message instanceof PunterRestartMessage) {
            restartClient();
        } else if (message instanceof PunterProcessRunMessage) {
            try {
                if (((PunterProcessRunMessage) message).getHostname().equalsIgnoreCase(getLocalHostAddress().getHostName())) {
                    PunterJobBasket.getInstance().addJobToBasket((PunterProcessRunMessage) message);
                }
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void restartClient() {
       //this will not work
    }

    @Override
    public PunterMessage getMessage() throws InterruptedException, RemoteException {
        return stub.getMessage(getSessionId());
    }

    @Override
    public void ping() throws RemoteException {
//        stub.ping(getSessionId());
    }

    @Override
    public InetAddress getServerHostAddress() throws Exception {
        return stub.getServerHostAddress();
    }

    @Override
    public InetAddress getLocalHostAddress() throws UnknownHostException {
        return InetAddress.getLocalHost();
    }

    @Override
    public long getWebServerPort() throws RemoteException {
        return stub.getWebServerPort();
    }

    public StaticDaoFacadeLocal() {
        singleInstanceFileLock = new SingleInstanceFileLock("PunterServer.lock");
        sessionFacade = SessionFacade.getInstance();
        transatomatic = new JPATransatomatic(new JPASessionFactory());
        serverSettings = new ServerSettings();
        staticDaoFacade = new com.shunya.server.StaticDaoFacade(transatomatic);
        serverSettings.setStaticDaoFacade(staticDaoFacade);
        staticDaoFacade.setSettings(serverSettings);
        context = new ServerContext(staticDaoFacade, sessionFacade, transatomatic, serverSettings);
        stub = new PunterSearchServer(staticDaoFacade, sessionFacade, serverSettings);
        staticDaoFacade.buildSynonymsCacheLocal();
    }

    @Override
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public synchronized void makeConnection() {
         //nothing to make connection to
    }

    @Override
    public List<String> getCategories() {
        try {
            return stub.getCategories();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getAllTerms() throws RemoteException {
        return stub.getAllTerms();
    }

    @Override
    public void updateAccessCounter(Document doc) throws RemoteException {
        stub.updateAccessCounter(doc);
    }

    @Override
    public Document createDocument(String author) throws RemoteException {
        return stub.createDocument(author);
    }

    @Override
    public List<Document> getDocList(SearchQuery searchQuery) throws RemoteException {
        try {
            return stub.getDocList(searchQuery);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public void deleteAllForCategory(String category) throws IOException {
        stub.deleteAllForCategory(category);
    }

    @Override
    public Document saveDocument(Document doc) throws RemoteException {
        return stub.saveDocument(doc);
    }

    @Override
    public Attachment saveAttachment(Attachment attach) throws RemoteException {
        return stub.saveAttachment(attach);
    }

    @Override
    public Document getDocument(Document doc) {
        return stub.getDocument(doc);
    }

    @Override
    public Attachment getAttachment(Attachment doc) {
        return staticDaoFacade.getAttachment(doc);
    }

    @Override
    public boolean deleteAttachment(Attachment attch) throws RemoteException {
        return stub.deleteAttachment(attch);
    }

    @Override
    public boolean deleteDocument(Document attch) throws RemoteException {
        return stub.deleteDocument(attch);
    }

    @Override
    public void rebuildIndex() throws RemoteException {
        stub.rebuildIndex();
    }

    @Override
    public void removeTask(TaskData task) {
        try {
            stub.removeTask(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void removeProcess(ProcessData proc) {
        try {
            stub.removeProcess(proc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public TaskData createTask(TaskData task) {
        try {
            return stub.createTask(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public ProcessData createProcess(ProcessData proc) {
        try {
            return stub.createProcess(proc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return proc;
    }


    @Override
    public ProcessHistory createProcessHistory(ProcessHistory ph) {
        try {
            return stub.createProcessHistory(ph);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public TaskHistory createTaskHistory(TaskHistory th) {
        try {
            return stub.createTaskHistory(th);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void saveTaskHistory(TaskHistory t) {
        try {
            stub.saveTaskHistory(t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void saveProcessHistory(ProcessHistory procHistory) {
        try {
            stub.saveProcessHistory(procHistory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public TaskData saveTask(TaskData t) throws Exception {
        try {
            return stub.saveTask(t);
        } catch (Exception e) {
            throw e;
        }
    }


    @Override
    public ProcessData saveProcess(ProcessData p) {
        try {
            return stub.saveProcess(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void listTask(long id) {
        try {
            stub.listTask(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public List<ProcessData> getScheduledProcessList(String username) {
        try {
            return stub.getScheduledProcessList(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }


    @Override
    public List<ProcessData> getProcessList(String username) throws Exception {
        try {
            return stub.getProcessList(username);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    @Override
    public ProcessData getProcess(long id) {
        try {
            return stub.getProcess(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public TaskHistory getTaskDao(TaskHistory td) {
        try {
            return stub.getTaskDao(td);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public List<ProcessHistory> getProcessHistoryListForProcessId(long id) {
        try {
            return stub.getProcessHistoryListForProcessId(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public List<ProcessHistory> getSortedProcessHistoryListForProcessId(long id) {
        try {
            return stub.getSortedProcessHistoryListForProcessId(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ProcessHistory> getMySortedProcessHistoryList(String username) {
        try {
            return stub.getMySortedProcessHistoryList(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public ProcessHistory getProcessHistoryById(long id) {
        try {
            return stub.getProcessHistoryById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public List<TaskData> getProcessTasksById(long pid) {
        try {
            return stub.getProcessTasksById(pid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }


    @Override
    public List<TaskData> getSortedTasksByProcessId(long pid) {
        try {
            return stub.getSortedTasksByProcessId(pid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<TaskData> getProcessTasks(long pid) {
        try {
            return stub.getProcessTasks(pid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public void disconnect() {
        //do nothing to disconnect
    }

    @Override
    public void sendMessageToPeer(PunterMessage punterMessage) throws InterruptedException, RemoteException {
        stub.sendMessage(getSessionId(), punterMessage, getUsername());
    }

    @Override
    public String getDevEmailCSV() throws RemoteException {
        return stub.getDevEmailCSV();
    }
}
