package com.sapient.server;

import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.List;

import com.sapient.kb.gui.SearchQuery;
import com.sapient.kb.jpa.Attachment;
import com.sapient.kb.jpa.Document;
import com.sapient.punter.jpa.ProcessData;
import com.sapient.punter.jpa.ProcessHistory;
import com.sapient.punter.jpa.TaskData;
import com.sapient.punter.jpa.TaskHistory;

public class PunterSearchServer implements PunterSearch {
	private StaticDaoFacade sdf;

	public PunterSearchServer() {
		sdf = StaticDaoFacade.getInstance();
	}


	@Override
	public void updateAccessCounter(Document doc) {
		sdf.updateAccessCounter(doc);
	}

	@Override
	public Document createDocument(String author) {
		return sdf.createDocument(author);
	}

	@Override
	public List<Document> getDocList(SearchQuery query) {
		return sdf.getDocList(query);
	}

	@Override
	public Document saveDocument(Document doc) {
		return sdf.saveDocument(doc);
	}

	@Override
	public Attachment saveAttachment(Attachment attach) {
		return sdf.saveAttachment(attach);
	}

	@Override
	public Document getDocument(Document doc) {
		return sdf.getDocument(doc);
	}

	@Override
	public boolean deleteAttachment(Attachment attch) {
		return sdf.deleteAttachment(attch);
	}

	@Override
	public boolean deleteDocument(Document attch) {
		return sdf.deleteDocument(attch);
	}

	@Override
	public void rebuildIndex() {
		sdf.rebuildIndex();
	}

	@Override
	public List<String> getCategories() throws RemoteException {
		return sdf.getCategories();
	}

	@Override
	public void ping() throws RemoteException {
		
	}

	@Override
	public void removeTask(TaskData task) throws Exception {
			sdf.removeTask(task);
	}

	@Override
	public void removeProcess(ProcessData processData) throws Exception {
			sdf.removeProcess(processData);
	}

	@Override
	public TaskData createTask(TaskData task) throws Exception {
			return sdf.createTask(task);
	}

	@Override
	public ProcessData createProcess(ProcessData proc) throws Exception {
			return sdf.createProcess(proc);
	}

	@Override
	public ProcessHistory createProcessHistory(ProcessHistory ph) throws Exception {
			return sdf.createProcessHistory(ph);
	}

	@Override
	public TaskHistory createTaskHistory(TaskHistory th) throws Exception {
			return sdf.createTaskHistory(th);
	}

	@Override
	public void saveTaskHistory(TaskHistory t) throws Exception {
			sdf.saveTaskHistory(t);
	}

	@Override
	public void saveProcessHistory(ProcessHistory procHistory) throws Exception {
				sdf.saveProcessHistory(procHistory);
	}

	@Override
	public TaskData saveTask(TaskData t) throws Exception {
			return sdf.saveTask(t);
	}

	@Override
	public ProcessData saveProcess(ProcessData p) throws Exception {
			return sdf.saveProcess(p);
	}

	@Override
	public void listTask(long id) throws Exception {
			sdf.listTask(id);
	}

	@Override
	public List<ProcessData> getScheduledProcessList(String username) throws Exception {
			return sdf.getScheduledProcessList(username);
	}

	@Override
	public List<ProcessData> getProcessList(String username) throws Exception {
			return sdf.getProcessList(username);
	}

	@Override
	public ProcessData getProcess(long id) throws Exception {
			return sdf.getProcess(id);
	}

	@Override
	public TaskHistory getTaskDao(TaskHistory td) throws Exception {
			return sdf.getTaskDao(td);
	}

	@Override
	public List<ProcessHistory> getProcessHistoryListForProcessId(long id)
            throws Exception {
			return sdf.getProcessHistoryListForProcessId(id);
	}

	@Override
	public List<ProcessHistory> getSortedProcessHistoryListForProcessId(long id)
            throws Exception {
			return sdf.getSortedProcessHistoryListForProcessId(id);
	}

	@Override
	public ProcessHistory getProcessHistoryById(long id) throws Exception {
			return sdf.getProcessHistoryById(id);
	}

	@Override
	public List<TaskData> getProcessTasksById(long pid) throws Exception {
			return sdf.getProcessTasksById(pid);
	}

	@Override
	public List<TaskData> getSortedTasksByProcessId(long pid)
            throws Exception {
			return sdf.getSortedTasksByProcessId(pid);
	}

	@Override
	public List<TaskData> getProcessTasks(long pid) throws Exception {
			return sdf.getProcessTasks(pid);
	}

	@Override
	public void deleteTeam() throws RemoteException {
		sdf.deleteTeam();
	}

	@Override
	public List<ProcessHistory> getMySortedProcessHistoryList(String username)
			throws RemoteException {
			return sdf.getMySortedProcessHistoryList(username);
	}

	@Override
	public InetAddress getServerHostAddress() throws RemoteException, UnknownHostException {
			return InetAddress.getLocalHost();
	}

	@Override
	public long getWebServerPort() {
		return ServerSettings.getInstance().getWebServerPort();
	}
	
}