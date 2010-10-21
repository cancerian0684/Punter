package com.sapient.kb.jpa;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

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
	private StaticDaoFacade() {
		try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            stub = (PunterSearch) registry.lookup("PunterSearch");
//            String response = stub.sayHello();
//            System.out.println("response: " + response);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
	}

  public void updateAccessCounter(Document doc) throws RemoteException{
	  stub.updateAccessCounter(doc);
  }
  public Document createDocument() throws RemoteException{
	  return stub.createDocument();
  }
  public List<Document> getDocList(String q,String category,boolean isSpclTxt,boolean isAND) throws RemoteException{
	  return stub.getDocList(q, category, isSpclTxt, isAND);
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
}
