package com.shunya.server;

import com.shunya.kb.gui.SearchQuery;
import com.shunya.kb.jpa.Attachment;
import com.shunya.kb.jpa.Document;
import com.shunya.punter.jpa.ProcessData;
import com.shunya.punter.jpa.ProcessHistory;
import com.shunya.punter.jpa.TaskData;
import com.shunya.punter.jpa.TaskHistory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface PunterSearch extends Remote {
    List<String> getAllTerms() throws RemoteException;

    InetAddress getServerHostAddress() throws RemoteException, UnknownHostException;

    long getWebServerPort() throws RemoteException;

    String connect(String username) throws RemoteException;

    void disconnect(String sessionId) throws RemoteException;

    void ping(String sessionId) throws RemoteException;

    void updateAccessCounter(Document doc) throws RemoteException;

    Document createDocument(String author) throws RemoteException;

    List<Document> getDocList(SearchQuery searchQuery) throws RemoteException;

    Document saveDocument(Document doc) throws RemoteException;

    Attachment saveAttachment(Attachment attach) throws RemoteException;

    Document getDocument(Document doc) throws RemoteException;

    boolean deleteAttachment(Attachment attch) throws RemoteException;

    boolean deleteDocument(Document attch) throws RemoteException;

    void rebuildIndex() throws RemoteException;

    List<String> getCategories() throws RemoteException;

    void removeTask(TaskData task) throws Exception;

    void removeProcess(ProcessData proc) throws Exception;

    TaskData createTask(TaskData task) throws Exception;

    ProcessData createProcess(ProcessData proc) throws Exception;

    ProcessHistory createProcessHistory(ProcessHistory ph) throws Exception;

    TaskHistory createTaskHistory(TaskHistory th) throws Exception;

    void saveTaskHistory(TaskHistory t) throws Exception;

    void saveProcessHistory(ProcessHistory procHistory) throws Exception;

    TaskData saveTask(TaskData t) throws Exception;

    ProcessData saveProcess(ProcessData p) throws Exception;

    void listTask(long id) throws Exception;

    List<ProcessData> getScheduledProcessList(String username) throws Exception;

    List<ProcessData> getProcessList(String username) throws Exception;

    ProcessData getProcess(long id) throws Exception;

    TaskHistory getTaskDao(TaskHistory td) throws Exception;

    List<ProcessHistory> getProcessHistoryListForProcessId(long id) throws Exception;

    List<ProcessHistory> getSortedProcessHistoryListForProcessId(long id) throws Exception;

    public List<ProcessHistory> getMySortedProcessHistoryList(String username) throws RemoteException;

    ProcessHistory getProcessHistoryById(long id) throws Exception;

    List<TaskData> getProcessTasksById(long pid) throws Exception;

    List<TaskData> getSortedTasksByProcessId(long pid) throws Exception;

    List<TaskData> getProcessTasks(long pid) throws Exception;

    void sendMessage(String sessionId, PunterMessage punterMessage) throws RemoteException, InterruptedException;

    void sendMessage(String sessionId, PunterMessage punterMessage,String topic) throws RemoteException, InterruptedException;

    PunterMessage getMessage(String sessionId) throws InterruptedException,RemoteException;

    String getDevEmailCSV() throws RemoteException;

    void sendMessageToAll(String sessionId, PunterMessage punterMessage) throws RemoteException, InterruptedException;

    String getJNLPURL() throws UnknownHostException, RemoteException;
}
