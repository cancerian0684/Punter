package com.sapient.kb.jpa;
import java.net.InetAddress;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.derby.drda.NetworkServerControl;

import com.sapient.kb.gui.LuceneIndexDao;

public class StaticDaoFacade {
	private static LuceneIndexDao luceneIndexDao;
	private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("punter");
	static{
		try{
		final NetworkServerControl serverControl = new NetworkServerControl(InetAddress.getByName("localhost"), 1527);
		try{
			serverControl.getRuntimeInfo();
		}catch (Exception e) {
			serverControl.start(null);
		}
		Thread.currentThread();
		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run() {
				 try {
					 emf.close();
//					serverControl.shutdown();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
  public static void main(String[] a) throws Exception {
    EntityManager em = emf.createEntityManager();
    DocumentService service = new DocumentService(em);

    em.getTransaction().begin();

    service.createDocument("name");
    System.out.println("Professors:");
    for (Document emp : service.findAllDocuments()) {
      System.out.println(emp);
    }
    em.getTransaction().commit();
    em.close();
  }
  public static void updateAccessCounter(Document doc){
		EntityManager em = emf.createEntityManager();
	    DocumentService service = new DocumentService(em);
	    em.getTransaction().begin();
	    service.updateAccessCounter(doc);
	    em.getTransaction().commit();
	    em.close();
  }
  public static Document createDocument(){
	  	EntityManager em = emf.createEntityManager();
	    DocumentService service = new DocumentService(em);

	    em.getTransaction().begin();

	    Document doc = service.createDocument("test title","");
	    luceneIndexDao.getInstance().indexDocs(doc);
	    em.getTransaction().commit();
	    em.close();
	    return doc;
  }
  public static List<Document> getDocList(String q,String category,boolean isSpclTxt){
	   long t1=System.currentTimeMillis();
	   List<Document> result = luceneIndexDao.getInstance().search(q, category,isSpclTxt,0, 100);
	   long t2=System.currentTimeMillis();
	   System.err.println("time consumed : "+(t2-t1));
	   return result;
  }
  public static Document saveDocument(Document doc){
	  	EntityManager em = emf.createEntityManager();
	  	DocumentService service = new DocumentService(em);
	    em.getTransaction().begin();
	    service.saveDocument(doc);
	    em.getTransaction().commit();
	    em.close();
	    luceneIndexDao.getInstance().indexDocs(doc);
	    return doc;
  }
  public static Attachment saveAttachment(Attachment attach){
	  	EntityManager em = emf.createEntityManager();
	  	DocumentService service = new DocumentService(em);
	    em.getTransaction().begin();
	    service.saveAttachment(attach);
	    em.getTransaction().commit();
	    em.close();
	    luceneIndexDao.getInstance().indexDocs(attach.getDocument());
	    return attach;
}
  public static Document getDocument(Document doc){
	  	EntityManager em = emf.createEntityManager();
	  	try{
	  	DocumentService service = new DocumentService(em);
	  	doc=service.getDocument(doc);
	  	}finally{
	    em.close();
	  	}
	    return doc;
}
public static boolean deleteAttachment(Attachment attch) {
	EntityManager em = emf.createEntityManager();
	em.getTransaction().begin();
  	DocumentService service = new DocumentService(em);
  	service.deleteAttachment(attch);
  	em.getTransaction().commit();
    em.close();
    luceneIndexDao.getInstance().indexDocs(attch.getDocument());
	return true;
}
public static boolean deleteDocument(Document attch) {
	EntityManager em = emf.createEntityManager();
	em.getTransaction().begin();
  	DocumentService service = new DocumentService(em);
  	service.deleteDocument(attch);
  	em.getTransaction().commit();
    em.close();
    LuceneIndexDao.getInstance().deleteIndexForDoc(attch);
	return true;
}
public static void rebuildIndex(){
	EntityManager em = emf.createEntityManager();
	em.getTransaction().begin();
  	DocumentService service = new DocumentService(em);
    for (Document emp : service.findAllDocuments()) {
      System.out.println(emp.getCategory());
      luceneIndexDao.getInstance().indexDocs(emp);
    }
    em.close();
}
}
