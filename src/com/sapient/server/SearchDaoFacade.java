package com.sapient.server;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;

import org.apache.derby.drda.NetworkServerControl;

import com.sapient.kb.jpa.Attachment;
import com.sapient.kb.jpa.Document;
import com.sapient.kb.utils.TestEditor;

public class SearchDaoFacade {
	private static SearchDaoFacade sdf;
	private EntityManagerFactory emf;
	private EntityManager em;
	public static SearchDaoFacade getInstance(){
		if(sdf==null){
			sdf=new SearchDaoFacade();
		}
		return sdf;
	}
	private SearchDaoFacade() {
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
						em.close();
						emf.close();
					    serverControl.shutdown();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			emf = Persistence.createEntityManagerFactory("punter");
			em = emf.createEntityManager();
			em.setFlushMode(FlushModeType.COMMIT);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	    
  public List<String> getCategories(){
	List<String> categories=new ArrayList<String>(20);
	Scanner scanner = new Scanner(TestEditor.class.getClassLoader().getResourceAsStream("resources/categories"));
    while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        StringTokenizer stk=new StringTokenizer(line, ",");
        while (stk.hasMoreTokens()) {
        	categories.add(stk.nextToken());
		}
    }
    scanner.close();
    return categories;
  }
  public void updateAccessCounter(Document doc){
		EntityManager em = emf.createEntityManager();
	    DocumentService service = new DocumentService(em);
	    em.getTransaction().begin();
	    service.updateAccessCounter(doc);
	    em.getTransaction().commit();
	    em.close();
  }
  public Document createDocument(){
	  	EntityManager em = emf.createEntityManager();
	    DocumentService service = new DocumentService(em);

	    em.getTransaction().begin();

	    Document doc = service.createDocument("test title","");
	    LuceneIndexDao.getInstance().indexDocs(doc);
	    em.getTransaction().commit();
	    em.close();
	    return doc;
  }
  public List<Document> getDocList(String q,String category,boolean isSpclTxt,boolean isAND){
	   long t1=System.currentTimeMillis();
	   List<Document> result = LuceneIndexDao.getInstance().search(q, category,isSpclTxt,isAND,0, 25);
	   long t2=System.currentTimeMillis();
	   System.err.println("time consumed : "+(t2-t1));
	   return result;
  }
  public Document saveDocument(Document doc){
	  	EntityManager em = emf.createEntityManager();
	  	DocumentService service = new DocumentService(em);
	    em.getTransaction().begin();
	    service.saveDocument(doc);
	    em.getTransaction().commit();
	    em.close();
	    LuceneIndexDao.getInstance().indexDocs(doc);
	    return doc;
  }
  public Attachment saveAttachment(Attachment attach){
	  	EntityManager em = emf.createEntityManager();
	  	DocumentService service = new DocumentService(em);
	    em.getTransaction().begin();
	    service.saveAttachment(attach);
	    Document doc=attach.getDocument();
	    em.getTransaction().commit();
	    doc=em.find(Document.class, doc.getId());
	    em.close();
	    LuceneIndexDao.getInstance().indexDocs(doc);
	    return attach;
}
  public Document getDocument(Document doc){
//	  	EntityManager em = emf.createEntityManager();
	  	try{
	  	DocumentService service = new DocumentService(em);
	  	doc=service.getDocument(doc);
	  	}finally{
//	    em.close();
	  	}
	    return doc;
}
public boolean deleteAttachment(Attachment attch) {
	EntityManager em = emf.createEntityManager();
	em.getTransaction().begin();
  	DocumentService service = new DocumentService(em);
  	service.deleteAttachment(attch);
  	Document doc=attch.getDocument();
  	em.getTransaction().commit();
  	doc=em.find(Document.class, doc.getId());
    em.close();
    LuceneIndexDao.getInstance().indexDocs(doc);
	return true;
}
public boolean deleteDocument(Document attch) {
	EntityManager em = emf.createEntityManager();
	em.getTransaction().begin();
  	DocumentService service = new DocumentService(em);
  	service.deleteDocument(attch);
  	em.getTransaction().commit();
    em.close();
    LuceneIndexDao.getInstance().deleteIndexForDoc(attch);
	return true;
}
public void rebuildIndex(){
	System.out.println("Clearing old index");
	LuceneIndexDao.getInstance().deleteIndex();
	EntityManager em = emf.createEntityManager();
	em.getTransaction().begin();
  	DocumentService service = new DocumentService(em);
    for (Document emp : service.findAllDocuments()) {
      System.out.println(emp.getCategory());
      LuceneIndexDao.getInstance().indexDocs(emp);
    }
    em.close();
}

}
