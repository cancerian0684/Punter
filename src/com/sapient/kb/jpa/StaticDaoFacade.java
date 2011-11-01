package com.sapient.kb.jpa;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.sapient.kb.gui.SearchQuery;
import com.sapient.punter.gui.AppSettings;
import com.sapient.punter.jpa.ProcessData;
import com.sapient.punter.jpa.ProcessHistory;
import com.sapient.punter.jpa.TaskData;
import com.sapient.punter.jpa.TaskHistory;
import com.sapient.punter.utils.ClipBoardListener;
import com.sapient.server.ClipboardPunterMessage;
import com.sapient.server.PunterMessage;
import com.sapient.server.PunterRestartMessage;
import com.sapient.server.PunterSearch;

import javax.swing.*;

public class StaticDaoFacade {
    private static StaticDaoFacade sdf;
    private PunterSearch stub;
    private ClipBoardListener clipBoardListener = new ClipBoardListener();


    public String getSessionId() {
        return sessionId;
    }

    private String sessionId;

    public String getUsername() {
        return AppSettings.getInstance().getUsername();
    }

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
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    private void process(PunterMessage message) {
        if (message instanceof ClipboardPunterMessage) {
            clipBoardListener.handleContent((ClipboardPunterMessage) message);
        } else if (message instanceof PunterRestartMessage) {
            restartClient();
        }

    }

    public void restartClient() {
        try {
            String url=stub.getJNLPURL();
            Runtime.getRuntime().exec("javaws "+url);
            System.exit(0);
        } catch (UnknownHostException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (RemoteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public PunterMessage getMessage() throws InterruptedException, RemoteException {
        return stub.getMessage(getSessionId());
    }

    public static StaticDaoFacade getInstance() {
        if (sdf == null) {
            sdf = new StaticDaoFacade();
        }
        return sdf;
    }

    public void ping() throws RemoteException {
        stub.ping(getSessionId());
    }

    public InetAddress getServerHostAddress() throws Exception {
        return stub.getServerHostAddress();
    }

    public long getWebServerPort() throws RemoteException {
        return stub.getWebServerPort();
    }

    private StaticDaoFacade() {
        makeConnection();
        messageProcessor();
        clipBoardListener.start();
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void makeConnection() {
        String defaultHost = "127.0.0.1";
        if (AppSettings.getInstance().getServerHost() == null || AppSettings.getInstance().getServerHost().isEmpty()) {
            if (AppSettings.getInstance().isMultiSearchEnable()) {
                MultiCastServerLocator mcsl = new MultiCastServerLocator();
                defaultHost = mcsl.LocateServerAddress();
            }
            defaultHost = JOptionPane.showInputDialog("Enter Server IP Address : ", defaultHost);
            AppSettings.getInstance().setServerHost(defaultHost);
        }
        try {
            Registry registry = null;
            try {
                registry = LocateRegistry.getRegistry(AppSettings.getInstance().getServerHost());
            } catch (Exception e) {
                e.printStackTrace();
                AppSettings.getInstance().setServerHost(null);
            }
            stub = (PunterSearch) registry.lookup("PunterSearch");
            if (getSessionId() == null) {
                setSessionId(stub.connect(getUsername()));
            }
            stub.ping(getSessionId());
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            AppSettings.getInstance().setServerHost(null);
//			e.printStackTrace();
        }
    }

    public List<String> getCategories() {
        try {
            return stub.getCategories();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public void updateAccessCounter(Document doc) throws RemoteException {
        stub.updateAccessCounter(doc);
    }

    public Document createDocument(String author) throws RemoteException {
        return stub.createDocument(author);
    }

    public List<Document> getDocList(SearchQuery searchQuery) throws RemoteException {
        return stub.getDocList(searchQuery);
    }

    public Document saveDocument(Document doc) throws RemoteException {
        return stub.saveDocument(doc);
    }

    public Attachment saveAttachment(Attachment attach) throws RemoteException {
        return stub.saveAttachment(attach);
    }

    public Document getDocument(Document doc) throws RemoteException {
        return stub.getDocument(doc);
    }

    public boolean deleteAttachment(Attachment attch) throws RemoteException {
        return stub.deleteAttachment(attch);
    }

    public boolean deleteDocument(Document attch) throws RemoteException {
        return stub.deleteDocument(attch);
    }

    public void rebuildIndex() throws RemoteException {
        stub.rebuildIndex();
    }

    public void removeTask(TaskData task) {
        try {
            stub.removeTask(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void removeProcess(ProcessData proc) {
        try {
            stub.removeProcess(proc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public TaskData createTask(TaskData task) {
        try {
            return stub.createTask(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public ProcessData createProcess(ProcessData proc) {
        try {
            return stub.createProcess(proc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return proc;
    }


    public ProcessHistory createProcessHistory(ProcessHistory ph) {
        try {
            return stub.createProcessHistory(ph);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public TaskHistory createTaskHistory(TaskHistory th) {
        try {
            return stub.createTaskHistory(th);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void saveTaskHistory(TaskHistory t) {
        try {
            stub.saveTaskHistory(t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void saveProcessHistory(ProcessHistory procHistory) {
        try {
            stub.saveProcessHistory(procHistory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public TaskData saveTask(TaskData t) throws Exception {
        try {
            return stub.saveTask(t);
        } catch (Exception e) {
            throw e;
        }
    }


    public ProcessData saveProcess(ProcessData p) {
        try {
            return stub.saveProcess(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void listTask(long id) {
        try {
            stub.listTask(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public List<ProcessData> getScheduledProcessList(String username) {
        try {
            return stub.getScheduledProcessList(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }


    public List<ProcessData> getProcessList(String username) throws Exception {
        try {
            return stub.getProcessList(username);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    public ProcessData getProcess(long id) {
        try {
            return stub.getProcess(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public TaskHistory getTaskDao(TaskHistory td) {
        try {
            return stub.getTaskDao(td);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<ProcessHistory> getProcessHistoryListForProcessId(long id) {
        try {
            return stub.getProcessHistoryListForProcessId(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<ProcessHistory> getSortedProcessHistoryListForProcessId(long id) {
        try {
            return stub.getSortedProcessHistoryListForProcessId(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ProcessHistory> getMySortedProcessHistoryList(String username) {
        try {
            return stub.getMySortedProcessHistoryList(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public ProcessHistory getProcessHistoryById(long id) {
        try {
            return stub.getProcessHistoryById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<TaskData> getProcessTasksById(long pid) {
        try {
            return stub.getProcessTasksById(pid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }


    public List<TaskData> getSortedTasksByProcessId(long pid) {
        try {
            return stub.getSortedTasksByProcessId(pid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    public List<TaskData> getProcessTasks(long pid) {
        try {
            return stub.getProcessTasks(pid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    public void disconnect() {
        try {
            stub.disconnect(getSessionId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToPeer(PunterMessage punterMessage) throws InterruptedException, RemoteException {
        stub.sendMessage(getSessionId(), punterMessage, getUsername());
    }

    public String getDevEmailCSV() throws RemoteException {
        return stub.getDevEmailCSV();
    }
}
