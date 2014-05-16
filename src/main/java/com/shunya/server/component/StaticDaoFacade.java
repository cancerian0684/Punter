package com.shunya.server.component;

import com.shunya.kb.gui.SearchQuery;
import com.shunya.kb.jpa.Attachment;
import com.shunya.kb.jpa.Document;
import com.shunya.punter.jpa.ProcessData;
import com.shunya.punter.jpa.ProcessHistory;
import com.shunya.punter.jpa.TaskData;
import com.shunya.punter.jpa.TaskHistory;
import com.shunya.punter.utils.ClipBoardListener;
import com.shunya.server.PunterMessage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.List;

public interface StaticDaoFacade {
    void setClipBoardListener(ClipBoardListener clipBoardListener);

    String getSessionId();

    String getUsername();

    void messageProcessor();

    void restartClient();

    PunterMessage getMessage() throws InterruptedException, RemoteException;

    void ping() throws RemoteException;

    InetAddress getServerHostAddress() throws Exception;

    InetAddress getLocalHostAddress() throws UnknownHostException;

    long getWebServerPort() throws RemoteException;

    void setSessionId(String sessionId);

    void makeConnection();

    List<String> getCategories();

    List<String> getAllTerms() throws RemoteException;

    void updateAccessCounter(Document doc) throws RemoteException;

    Document createDocument(String author) throws RemoteException;

    List<Document> getDocList(SearchQuery searchQuery) throws RemoteException;

    void deleteAllForCategory(String category) throws IOException;

    Document saveDocument(Document doc) throws RemoteException;

    Attachment saveAttachment(Attachment attach) throws RemoteException;

    Document getDocument(Document doc);

    Attachment getAttachment(Attachment doc) throws RemoteException;

    boolean deleteAttachment(Attachment attch) throws RemoteException;

    boolean deleteDocument(Document attch) throws RemoteException;

    void rebuildIndex() throws RemoteException;

    void removeTask(TaskData task);

    void removeProcess(ProcessData proc);

    TaskData createTask(TaskData task);

    ProcessData createProcess(ProcessData proc);

    ProcessHistory createProcessHistory(ProcessHistory ph);

    TaskHistory createTaskHistory(TaskHistory th);

    void saveTaskHistory(TaskHistory t);

    void saveProcessHistory(ProcessHistory procHistory);

    TaskData saveTask(TaskData t) throws Exception;

    ProcessData saveProcess(ProcessData p);

    void listTask(long id);

    List<ProcessData> getScheduledProcessList(String username);

    List<ProcessData> getProcessList(String username) throws Exception;

    ProcessData getProcess(long id);

    TaskHistory getTaskDao(TaskHistory td);

    List<ProcessHistory> getProcessHistoryListForProcessId(long id);

    List<ProcessHistory> getSortedProcessHistoryListForProcessId(long id);

    List<ProcessHistory> getMySortedProcessHistoryList(String username);

    ProcessHistory getProcessHistoryById(long id);

    List<TaskData> getProcessTasksById(long pid);

    List<TaskData> getSortedTasksByProcessId(long pid);

    List<TaskData> getProcessTasks(long pid);

    void disconnect();

    void sendMessageToPeer(PunterMessage punterMessage) throws InterruptedException, RemoteException;

    String getDevEmailCSV() throws RemoteException;
}
