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

	private static String findHostName() {
		try {
			String hostname=InetAddress.getLocalHost().getHostName();
			System.err.println("Hostname : "+hostname);
			return hostname;
		} catch (UnknownHostException e) {
			return "localhost";
		}
	}


	public static void main(String args[]) {
		try {
			String codebaseURI = new File("bin/").toURL().toURI().toString();
			System.out.println("Codebase is :" + codebaseURI);
			System.setProperty("java.rmi.server.codebase", codebaseURI);
			System.setProperty("java.rmi.server.hostname", findHostName());
			// System.setProperty("java.rmi.server.name","munishc-2k8");
			System.setProperty("java.security.policy", "policy.all");
			System.out.println("Killing the already running RMI Registry");
			Runtime.getRuntime().exec("taskkill /IM RMIREGISTRY.EXE");
			Thread.sleep(2000);
			System.out.println("Starting the rmi registry");
			final Process proc = Runtime.getRuntime().exec("rmiregistry");
			Runtime.getRuntime().addShutdownHook(new Thread(){
				@Override
				public void run() {
					super.run();
					proc.destroy();
				}
			});
			Thread.sleep(1000);
			PunterSearch obj = new PunterSearchServer();
			PunterSearch stub = (PunterSearch) UnicastRemoteObject.exportObject(obj, 0);
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind("PunterSearch", stub);

			System.err.println("RMI Server ready");
			java.awt.Desktop.getDesktop().browse(new URI("http://localhost:8080/index.html")); 
			WebServer.main(new String[]{});
			System.err.println("Web Server ready");
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}

	@Override
	public void updateAccessCounter(Document doc) {
		sdf.updateAccessCounter(doc);
	}

	@Override
	public Document createDocument() {
		return sdf.createDocument();
	}

	@Override
	public List<Document> getDocList(String q, String category,
			boolean isSpclTxt, boolean isAND) {
		return sdf.getDocList(q, category, isSpclTxt, isAND);
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
	public void removeTask(TaskData task) throws RemoteException {
		try {
			sdf.removeTask(task);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void removeProcess(ProcessData proc) throws RemoteException {
		try {
			sdf.removeProcess(proc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public TaskData createTask(TaskData task) throws RemoteException {
		try {
			return sdf.createTask(task);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ProcessData createProcess(ProcessData proc) throws RemoteException {
		try {
			return sdf.createProcess(proc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return proc;
	}

	@Override
	public ProcessHistory createProcessHistory(ProcessHistory ph)
			throws RemoteException {
		try {
			return sdf.createProcessHistory(ph);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public TaskHistory createTaskHistory(TaskHistory th) throws RemoteException {
		try {
			return sdf.createTaskHistory(th);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void saveTaskHistory(TaskHistory t) throws RemoteException {
		try {
			sdf.saveTaskHistory(t);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void saveProcessHistory(ProcessHistory procHistory)
			throws RemoteException {
			try {
				sdf.saveProcessHistory(procHistory);
			} catch (Exception e) {
				e.printStackTrace();
			}		
	}

	@Override
	public TaskData saveTask(TaskData t) throws RemoteException {
		try {
			return sdf.saveTask(t);
		} catch (Exception e) {
			throw new RemoteException("", e);
		}
	}

	@Override
	public ProcessData saveProcess(ProcessData p) throws RemoteException {
		try {
			return sdf.saveProcess(p);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException("", e);
		}
	}

	@Override
	public void listTask(long id) throws RemoteException {
		try {
			sdf.listTask(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public List<ProcessData> getScheduledProcessList() throws RemoteException {
		try {
			return sdf.getScheduledProcessList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<ProcessData> getProcessList() throws RemoteException {
		try {
			return sdf.getProcessList();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException("", e);
		}
	}

	@Override
	public ProcessData getProcess(long id) throws RemoteException {
		try {
			return sdf.getProcess(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public TaskHistory getTaskDao(TaskHistory td) throws RemoteException {
		try {
			return sdf.getTaskDao(td);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<ProcessHistory> getProcessHistoryListForProcessId(long id)
			throws RemoteException {
		try {
			return sdf.getProcessHistoryListForProcessId(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<ProcessHistory> getSortedProcessHistoryListForProcessId(long id)
			throws RemoteException {
		try {
			return sdf.getSortedProcessHistoryListForProcessId(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ProcessHistory getProcessHistoryById(long id) throws RemoteException {
		try {
			return sdf.getProcessHistoryById(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<TaskData> getProcessTasksById(long pid) throws RemoteException {
		try {
			return sdf.getProcessTasksById(pid);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<TaskData> getSortedTasksByProcessId(long pid)
			throws RemoteException {
		try {
			return sdf.getSortedTasksByProcessId(pid);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<TaskData> getProcessTasks(long pid) throws RemoteException {
		try {
			return sdf.getProcessTasks(pid);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void deleteTeam() throws RemoteException {
		sdf.deleteTeam();
	}
	
}