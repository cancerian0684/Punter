package com.sapient.kb.jpa;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collections;
import java.util.List;

import com.sapient.punter.gui.AppSettings;
import com.sapient.punter.jpa.ProcessData;
import com.sapient.punter.jpa.ProcessHistory;
import com.sapient.punter.jpa.TaskData;
import com.sapient.punter.jpa.TaskHistory;
import com.sapient.server.PunterSearch;


public class StaticDaoFacade {
	private static StaticDaoFacade sdf;
	private PunterSearch stub;
	public static StaticDaoFacade getInstance(){
		if(sdf==null){
			sdf=new StaticDaoFacade();
		}
		return sdf;
	}
	public void ping() throws RemoteException  {
		stub.ping();
	}
	private StaticDaoFacade() {
		makeConnection();
	}
	public void makeConnection(){
		String host="localhost";
		try {
			if(AppSettings.getInstance().isMultiSearchEnable()){
				try{
				MultiCastServerLocator mcsl=new MultiCastServerLocator();
				host=mcsl.LocateServerAddress();}catch (Exception e) {e.printStackTrace();}
			}
			Registry registry = LocateRegistry.getRegistry(host);
			stub = (PunterSearch) registry.lookup("PunterSearch");
			stub.ping();
		} catch (Exception e) {
			System.err.println("Client exception: " + e.toString());
//			e.printStackTrace();
		}
	}
  public List<String> getCategories(){
	  try {
		return stub.getCategories();
	} catch (RemoteException e) {
		e.printStackTrace();
	}
	return null;
  }
  public void updateAccessCounter(Document doc) throws RemoteException{
	  stub.updateAccessCounter(doc);
  }
  public Document createDocument() throws RemoteException{
	  return stub.createDocument();
  }
  public List<Document> getDocList(String q,String category,boolean isSpclTxt,boolean isAND,int maxResults) throws RemoteException{
	  return stub.getDocList(q, category, isSpclTxt, isAND, maxResults);
  }
  public Document saveDocument(Document doc) throws RemoteException{
	  return stub.saveDocument(doc);
  }
  public Attachment saveAttachment(Attachment attach) throws RemoteException{
	  return stub.saveAttachment(attach);
  }
  public Document getDocument(Document doc) throws RemoteException{
	  return stub.getDocument(doc);
  }
	public boolean deleteAttachment(Attachment attch) throws RemoteException {
		return stub.deleteAttachment(attch);
	}
	public boolean deleteDocument(Document attch) throws RemoteException {
		return stub.deleteDocument(attch);
	}
	public void rebuildIndex() throws RemoteException{
		stub.rebuildIndex();
	}

	public void removeTask(TaskData task)  {
		try {
			stub.removeTask(task);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public void removeProcess(ProcessData proc)  {
		try {
			stub.removeProcess(proc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public TaskData createTask(TaskData task)  {
		try {
			return stub.createTask(task);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	public ProcessData createProcess(ProcessData proc)  {
		try {
			return stub.createProcess(proc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return proc;
	}

	
	public ProcessHistory createProcessHistory(ProcessHistory ph)
			 {
		try {
			return stub.createProcessHistory(ph);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	public TaskHistory createTaskHistory(TaskHistory th)  {
		try {
			return stub.createTaskHistory(th);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	public void saveTaskHistory(TaskHistory t)  {
		try {
			stub.saveTaskHistory(t);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public void saveProcessHistory(ProcessHistory procHistory)
			 {
			try {
				stub.saveProcessHistory(procHistory);
			} catch (Exception e) {
				e.printStackTrace();
			}		
	}

	
	public TaskData saveTask(TaskData t) throws Exception  {
		try {
			return stub.saveTask(t);
		} catch (Exception e) {
			throw e;
		}
	}

	
	public ProcessData saveProcess(ProcessData p)  {
		try {
			return stub.saveProcess(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	public void listTask(long id)  {
		try {
			stub.listTask(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	
	public List<ProcessData> getScheduledProcessList()  {
		try {
			return stub.getScheduledProcessList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Collections.EMPTY_LIST;
	}

	
	public List<ProcessData> getProcessList() throws Exception  {
		try {
			return stub.getProcessList();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	
	public ProcessData getProcess(long id)  {
		try {
			return stub.getProcess(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	public TaskHistory getTaskDao(TaskHistory td)  {
		try {
			return stub.getTaskDao(td);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	public List<ProcessHistory> getProcessHistoryListForProcessId(long id)
			 {
		try {
			return stub.getProcessHistoryListForProcessId(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	public List<ProcessHistory> getSortedProcessHistoryListForProcessId(long id)
			 {
		try {
			return stub.getSortedProcessHistoryListForProcessId(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	public ProcessHistory getProcessHistoryById(long id)  {
		try {
			return stub.getProcessHistoryById(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	public List<TaskData> getProcessTasksById(long pid)  {
		try {
			return stub.getProcessTasksById(pid);
		}  catch (Exception e) {
			e.printStackTrace();
		}
		return Collections.EMPTY_LIST;
	}

	
	public List<TaskData> getSortedTasksByProcessId(long pid)
			 {
		try {
			return stub.getSortedTasksByProcessId(pid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Collections.EMPTY_LIST;
	}
	
	public List<TaskData> getProcessTasks(long pid)  {
		try {
			return stub.getProcessTasks(pid);
		}  catch (Exception e) {
			e.printStackTrace();
		}
		return Collections.EMPTY_LIST;
	}
	
	public void deleteTeam()  {
		try {
			stub.deleteTeam();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
