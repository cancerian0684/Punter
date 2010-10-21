package com.sapient.server;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import com.sapient.kb.jpa.Attachment;
import com.sapient.kb.jpa.Document;

public class PunterSearchServer implements PunterSearch {
	private SearchDaoFacade sdf;

	public PunterSearchServer() {
		sdf = SearchDaoFacade.getInstance();
	}

	public static void main(String args[]) {
		try {
			String codebaseURI = new File("bin/").toURL().toURI().toString();
			System.out.println("Codebase is :" + codebaseURI);
			System.setProperty("java.rmi.server.codebase", codebaseURI);
			System.setProperty("java.rmi.server.hostname", "localhost");
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

			System.err.println("Server ready");
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
}