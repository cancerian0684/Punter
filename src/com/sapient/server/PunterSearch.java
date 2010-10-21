package com.sapient.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import com.sapient.kb.jpa.Attachment;
import com.sapient.kb.jpa.Document;

public interface PunterSearch extends Remote {
	 void updateAccessCounter(Document doc)throws RemoteException;
	 Document createDocument()throws RemoteException;
	 List<Document> getDocList(String q,String category,boolean isSpclTxt,boolean isAND)throws RemoteException;
	 Document saveDocument(Document doc)throws RemoteException;
	 Attachment saveAttachment(Attachment attach)throws RemoteException;
	 Document getDocument(Document doc)throws RemoteException;
	 boolean deleteAttachment(Attachment attch)throws RemoteException;
	 boolean deleteDocument(Document attch)throws RemoteException;
	 void rebuildIndex()throws RemoteException;
}
