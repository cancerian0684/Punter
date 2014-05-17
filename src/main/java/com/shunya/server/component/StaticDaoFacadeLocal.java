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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;

@Service
public class StaticDaoFacadeLocal implements StaticDaoFacade {
    private ClipBoardListener clipBoardListener;
    private SingleInstanceFileLock singleInstanceFileLock;
    private HibernateDaoFacade hibernateDaoFacade;
    private SessionFacade sessionFacade;
    private JPATransatomatic transatomatic;
    private ServerSettings serverSettings;
    private ServerContext context;

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
        return sessionFacade.getMessage(getSessionId());
    }

    @Override
    public void ping() throws RemoteException {
//        hibernateDaoFacade.ping(getSessionId());
    }

    @Override
    public InetAddress getServerHostAddress() throws Exception {
        return InetAddress.getLocalHost();
    }

    @Override
    public InetAddress getLocalHostAddress() throws UnknownHostException {
        return InetAddress.getLocalHost();
    }

    @Override
    public long getWebServerPort() throws RemoteException {
        return serverSettings.getWebServerPort();
    }

    public StaticDaoFacadeLocal() {
        singleInstanceFileLock = new SingleInstanceFileLock("PunterServer.lock");
        sessionFacade = SessionFacade.getInstance();
        transatomatic = new JPATransatomatic(new JPASessionFactory());
        serverSettings = new ServerSettings();
        hibernateDaoFacade = new HibernateDaoFacade(transatomatic);
        serverSettings.setHibernateDaoFacade(hibernateDaoFacade);
        hibernateDaoFacade.setSettings(serverSettings);
        context = new ServerContext(hibernateDaoFacade, sessionFacade, transatomatic, serverSettings);
//        hibernateDaoFacade = new PunterSearchServer(hibernateDaoFacade, sessionFacade, serverSettings);
        hibernateDaoFacade.buildSynonymsCacheLocal();
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
        return hibernateDaoFacade.getCategories();
    }

    @Override
    public List<String> getAllTerms() throws IOException {
        return hibernateDaoFacade.getAllTerms();
    }

    @Override
    public void updateAccessCounter(Document doc) throws RemoteException {
        hibernateDaoFacade.updateAccessCounter(doc);
    }

    @Override
    public Document createDocument(String author) throws RemoteException {
        return hibernateDaoFacade.createDocument(author);
    }

    @Override
    public List<Document> getDocList(SearchQuery searchQuery) throws RemoteException {
        try {
            return hibernateDaoFacade.getDocList(searchQuery);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public void deleteAllForCategory(String category) throws IOException {
        hibernateDaoFacade.deleteAllForCategory(category);
    }

    @Override
    public Document saveDocument(Document doc) throws RemoteException {
        return hibernateDaoFacade.saveDocument(doc);
    }

    @Override
    public Attachment saveAttachment(Attachment attach) throws RemoteException {
        return hibernateDaoFacade.saveAttachment(attach);
    }

    @Override
    public Document getDocument(Document doc) {
        return hibernateDaoFacade.getDocument(doc);
    }

    @Override
    public Attachment getAttachment(Attachment doc) {
        return hibernateDaoFacade.getAttachment(doc);
    }

    @Override
    public boolean deleteAttachment(Attachment attch) throws RemoteException {
        return hibernateDaoFacade.deleteAttachment(attch);
    }

    @Override
    public boolean deleteDocument(Document attch) throws RemoteException {
        return hibernateDaoFacade.deleteDocument(attch);
    }

    @Override
    public void rebuildIndex() throws RemoteException {
        hibernateDaoFacade.rebuildIndex();
    }

    @Override
    public void removeTask(TaskData task) {
        try {
            hibernateDaoFacade.removeTask(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void removeProcess(ProcessData proc) {
        try {
            hibernateDaoFacade.removeProcess(proc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public TaskData createTask(TaskData task) {
        try {
            return (TaskData) hibernateDaoFacade.save(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public ProcessData createProcess(ProcessData proc) {
        try {
            return (ProcessData) hibernateDaoFacade.save(proc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return proc;
    }


    @Override
    public ProcessHistory createProcessHistory(ProcessHistory ph) {
        try {
            return (ProcessHistory) hibernateDaoFacade.save(ph);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public TaskHistory createTaskHistory(TaskHistory th) {
        try {
            return (TaskHistory) hibernateDaoFacade.save(th);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void saveTaskHistory(TaskHistory t) {
        try {
            hibernateDaoFacade.saveTaskHistory(t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void saveProcessHistory(ProcessHistory procHistory) {
        try {
            hibernateDaoFacade.saveProcessHistory(procHistory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public TaskData saveTask(TaskData t) throws Exception {
        try {
            return hibernateDaoFacade.saveTask(t);
        } catch (Exception e) {
            throw e;
        }
    }


    @Override
    public ProcessData saveProcess(ProcessData p) {
        try {
            return hibernateDaoFacade.saveProcess(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void listTask(long id) {
        try {
            hibernateDaoFacade.listTask(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public List<ProcessData> getScheduledProcessList(String username) {
        try {
            return hibernateDaoFacade.getScheduledProcessList(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }


    @Override
    public List<ProcessData> getProcessList(String username) throws Exception {
        try {
            return hibernateDaoFacade.getProcessList(username);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    @Override
    public ProcessData getProcess(long id) {
        try {
            return hibernateDaoFacade.getProcess(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public TaskHistory getTaskDao(TaskHistory td) {
        try {
            return hibernateDaoFacade.getTaskDao(td);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public List<ProcessHistory> getProcessHistoryListForProcessId(long id) {
        try {
            return hibernateDaoFacade.getProcessHistoryListForProcessId(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public List<ProcessHistory> getSortedProcessHistoryListForProcessId(long id) {
        try {
            return hibernateDaoFacade.getSortedProcessHistoryListForProcessId(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ProcessHistory> getMySortedProcessHistoryList(String username) {
        try {
            return hibernateDaoFacade.getMySortedProcessHistoryList(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public ProcessHistory getProcessHistoryById(long id) {
        try {
            return hibernateDaoFacade.getProcessHistoryById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public List<TaskData> getProcessTasksById(long pid) {
        try {
            return hibernateDaoFacade.getProcessTasksById(pid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }


    @Override
    public List<TaskData> getSortedTasksByProcessId(long pid) {
        try {
            return hibernateDaoFacade.getSortedTasksByProcessId(pid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<TaskData> getProcessTasks(long pid) {
        try {
            return hibernateDaoFacade.getProcessTasks(pid);
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
        sessionFacade.sendMessage(getSessionId(), punterMessage, getUsername());
    }

    @Override
    public String getDevEmailCSV() throws RemoteException {
        return serverSettings.getDevEmailCSV();
    }
}
