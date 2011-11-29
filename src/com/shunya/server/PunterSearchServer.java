package com.shunya.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.List;

import com.shunya.kb.gui.SearchQuery;
import com.shunya.kb.jpa.Attachment;
import com.shunya.kb.jpa.Document;
import com.shunya.punter.jpa.ProcessData;
import com.shunya.punter.jpa.ProcessHistory;
import com.shunya.punter.jpa.TaskData;
import com.shunya.punter.jpa.TaskHistory;

public class PunterSearchServer implements PunterSearch {
    private StaticDaoFacade staticDaoFacade;
    private SessionFacade sessionFacade;

    public PunterSearchServer() {
        staticDaoFacade = StaticDaoFacade.getInstance();
        sessionFacade = SessionFacade.getInstance();
    }


    @Override
    public void updateAccessCounter(Document doc) {
        staticDaoFacade.updateAccessCounter(doc);
    }

    @Override
    public Document createDocument(String author) {
        return staticDaoFacade.createDocument(author);
    }

    @Override
    public List<Document> getDocList(SearchQuery query) {
        return staticDaoFacade.getDocList(query);
    }

    @Override
    public Document saveDocument(Document doc) {
        return staticDaoFacade.saveDocument(doc);
    }

    @Override
    public Attachment saveAttachment(Attachment attach) {
        return staticDaoFacade.saveAttachment(attach);
    }

    @Override
    public Document getDocument(Document doc) {
        return staticDaoFacade.getDocument(doc);
    }

    @Override
    public String getDevEmailCSV() throws RemoteException {
        return ServerSettings.getInstance().getDevEmailCSV();
    }

    @Override
    public boolean deleteAttachment(Attachment attch) {
        return staticDaoFacade.deleteAttachment(attch);
    }

    @Override
    public boolean deleteDocument(Document attch) {
        return staticDaoFacade.deleteDocument(attch);
    }

    @Override
    public void rebuildIndex() {
        staticDaoFacade.rebuildIndex();
    }

    @Override
    public List<String> getCategories() throws RemoteException {
        return staticDaoFacade.getCategories();
    }

    @Override
    public void ping(String sessionId) throws RemoteException {
        sessionFacade.ping(sessionId);
    }

    @Override
    public void removeTask(TaskData task) throws Exception {
        staticDaoFacade.removeTask(task);
    }

    @Override
    public void removeProcess(ProcessData processData) throws Exception {
        staticDaoFacade.removeProcess(processData);
    }

    @Override
    public TaskData createTask(TaskData task) throws Exception {
        return staticDaoFacade.createTask(task);
    }

    @Override
    public ProcessData createProcess(ProcessData proc) throws Exception {
        return staticDaoFacade.createProcess(proc);
    }

    @Override
    public ProcessHistory createProcessHistory(ProcessHistory ph) throws Exception {
        return staticDaoFacade.createProcessHistory(ph);
    }

    @Override
    public TaskHistory createTaskHistory(TaskHistory th) throws Exception {
        return staticDaoFacade.createTaskHistory(th);
    }

    @Override
    public void saveTaskHistory(TaskHistory t) throws Exception {
        staticDaoFacade.saveTaskHistory(t);
    }

    @Override
    public void saveProcessHistory(ProcessHistory procHistory) throws Exception {
        staticDaoFacade.saveProcessHistory(procHistory);
    }

    @Override
    public TaskData saveTask(TaskData t) throws Exception {
        return staticDaoFacade.saveTask(t);
    }

    @Override
    public ProcessData saveProcess(ProcessData p) throws Exception {
        return staticDaoFacade.saveProcess(p);
    }

    @Override
    public void listTask(long id) throws Exception {
        staticDaoFacade.listTask(id);
    }

    @Override
    public List<ProcessData> getScheduledProcessList(String username) throws Exception {
        return staticDaoFacade.getScheduledProcessList(username);
    }

    @Override
    public List<ProcessData> getProcessList(String username) throws Exception {
        return staticDaoFacade.getProcessList(username);
    }

    @Override
    public ProcessData getProcess(long id) throws Exception {
        return staticDaoFacade.getProcess(id);
    }

    @Override
    public TaskHistory getTaskDao(TaskHistory td) throws Exception {
        return staticDaoFacade.getTaskDao(td);
    }

    @Override
    public List<ProcessHistory> getProcessHistoryListForProcessId(long id)
            throws Exception {
        return staticDaoFacade.getProcessHistoryListForProcessId(id);
    }

    @Override
    public List<ProcessHistory> getSortedProcessHistoryListForProcessId(long id)
            throws Exception {
        return staticDaoFacade.getSortedProcessHistoryListForProcessId(id);
    }

    @Override
    public ProcessHistory getProcessHistoryById(long id) throws Exception {
        return staticDaoFacade.getProcessHistoryById(id);
    }

    @Override
    public List<TaskData> getProcessTasksById(long pid) throws Exception {
        return staticDaoFacade.getProcessTasksById(pid);
    }

    @Override
    public List<TaskData> getSortedTasksByProcessId(long pid)
            throws Exception {
        return staticDaoFacade.getSortedTasksByProcessId(pid);
    }

    @Override
    public List<TaskData> getProcessTasks(long pid) throws Exception {
        return staticDaoFacade.getProcessTasks(pid);
    }

    @Override
    public void deleteTeam() throws RemoteException {
        staticDaoFacade.deleteTeam();
    }

    @Override
    public List<ProcessHistory> getMySortedProcessHistoryList(String username)
            throws RemoteException {
        return staticDaoFacade.getMySortedProcessHistoryList(username);
    }

    @Override
    public InetAddress getServerHostAddress() throws RemoteException, UnknownHostException {
        return InetAddress.getLocalHost();
    }

    @Override
    public long getWebServerPort() {
        return ServerSettings.getInstance().getWebServerPort();
    }

    @Override
    public String getJNLPURL() throws UnknownHostException, RemoteException {
        return "http://"+getServerHostAddress().getHostAddress()+":"+getWebServerPort()+"/punter.jnlp";
    }

    @Override
    public String connect(String username) throws RemoteException {
        return sessionFacade.getSession(username);
    }

    @Override
    public void disconnect(String sessionId) throws RemoteException {
        sessionFacade.removeSession(sessionId);
    }

    @Override
    public void sendMessage(String sessionId, PunterMessage punterMessage) throws RemoteException, InterruptedException {
        sessionFacade.sendMessage(sessionId, punterMessage);
    }

    @Override
    public void sendMessage(String sessionId, PunterMessage punterMessage, String topic) throws RemoteException, InterruptedException {
        sessionFacade.sendMessage(sessionId, punterMessage, topic);
    }

    @Override
    public void sendMessageToAll(String sessionId, PunterMessage punterMessage) throws RemoteException, InterruptedException {
        sessionFacade.sendMessageToAll(sessionId, punterMessage);
    }

    @Override
    public PunterMessage getMessage(String sessionId) throws InterruptedException, RemoteException {
        return sessionFacade.getMessage(sessionId);
    }

}