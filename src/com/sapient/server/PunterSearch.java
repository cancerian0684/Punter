package com.sapient.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import com.sapient.kb.jpa.Attachment;
import com.sapient.kb.jpa.Document;
import com.sapient.punter.jpa.ProcessData;
import com.sapient.punter.jpa.ProcessHistory;
import com.sapient.punter.jpa.TaskData;
import com.sapient.punter.jpa.TaskHistory;

public interface PunterSearch extends Remote {
	 void ping()throws RemoteException;
	 void updateAccessCounter(Document doc)throws RemoteException;
	 Document createDocument(String author)throws RemoteException;
	 List<Document> getDocList(String q,String category,boolean isSpclTxt,boolean isAND,int maxResults)throws RemoteException;
	 Document saveDocument(Document doc)throws RemoteException;
	 Attachment saveAttachment(Attachment attach)throws RemoteException;
	 Document getDocument(Document doc)throws RemoteException;
	 boolean deleteAttachment(Attachment attch)throws RemoteException;
	 boolean deleteDocument(Document attch)throws RemoteException;
	 void rebuildIndex()throws RemoteException;
	 List<String> getCategories()throws RemoteException;
	 
	 void removeTask(TaskData task)throws RemoteException;
	 void removeProcess(ProcessData proc)throws RemoteException;
	 TaskData createTask(TaskData task)throws RemoteException;
	 ProcessData createProcess(ProcessData proc)throws RemoteException;
	 ProcessHistory createProcessHistory(ProcessHistory ph)throws RemoteException;
	 TaskHistory createTaskHistory(TaskHistory th)throws RemoteException;
	 void saveTaskHistory(TaskHistory t)throws RemoteException;
	 void saveProcessHistory(ProcessHistory procHistory)throws RemoteException;
	 TaskData saveTask(TaskData t)throws RemoteException;
	 ProcessData saveProcess(ProcessData p)throws RemoteException;
	 void listTask(long id)throws RemoteException;
	 List<ProcessData> getScheduledProcessList(String username)throws RemoteException;
	 List<ProcessData> getProcessList(String username)throws RemoteException;
	 ProcessData getProcess(long id)throws RemoteException;
	 TaskHistory getTaskDao(TaskHistory td)throws RemoteException;
	 List<ProcessHistory> getProcessHistoryListForProcessId(long id)throws RemoteException;
	 List<ProcessHistory> getSortedProcessHistoryListForProcessId(long id)throws RemoteException;
	 public List<ProcessHistory> getMySortedProcessHistoryList(String username)throws RemoteException;
	 ProcessHistory getProcessHistoryById(long id)throws RemoteException;
	 List<TaskData> getProcessTasksById(long pid)throws RemoteException;
	 List<TaskData> getSortedTasksByProcessId(long pid)throws RemoteException;
	 List<TaskData> getProcessTasks(long pid)throws RemoteException;
	 void deleteTeam()throws RemoteException;
}
