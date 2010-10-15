package com.sapient.kb.jpa;
import java.net.InetAddress;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.apache.derby.drda.NetworkServerControl;

import com.sapient.kb.test.LuceneIndexDao;

public class StaticDaoFacade {
	private static LuceneIndexDao luceneIndexDao;
	private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("punter");
	static{
		try{
		/*final NetworkServerControl serverControl = new NetworkServerControl(InetAddress.getByName("localhost"), 1527);
		serverControl.start(null);*/
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
	    System.out.println("Documents:");
	    for (Document emp : service.findAllDocuments()) {
	      System.out.println(emp.getCategory());
	      luceneIndexDao.getInstance().indexDocs(emp);
	    }
	    em.getTransaction().commit();
	    em.close();
	    return doc;
  }
  public static List<Document> getDocList(String q){
	   long t1=System.currentTimeMillis();
	   List<Document> result = luceneIndexDao.getInstance().search(q, 0, 100);
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
  public static Document getDocument(Document doc){
	  	EntityManager em = emf.createEntityManager();
	  	DocumentService service = new DocumentService(em);
	  	doc=service.getDocument(doc);
	    em.close();
	    return doc;
}
}
