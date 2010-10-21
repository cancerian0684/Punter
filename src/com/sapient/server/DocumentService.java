package com.sapient.server;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.sapient.kb.jpa.Attachment;
import com.sapient.kb.jpa.Document;

public class DocumentService {
  protected EntityManager em;

  public DocumentService(EntityManager em) {
    this.em = em;
  }
  public Document createDocument(String title,String Content) {
	Document doc = new Document();
    doc.setTitle(title);
    doc.setContent(Content);
    doc.setDateCreated(new Date());
    doc.setCategory("/all");
    em.persist(doc);
    em.flush();
    return doc;
  }
  public void saveDocument(Document doc){
	  doc=em.merge(doc);
	  em.flush();
  }
  public void updateAccessCounter(Document doc){
	doc=em.find(Document.class, doc.getId());
	doc.setAccessCount(doc.getAccessCount()+1);
	doc.setDateAccessed(new Date());
	em.merge(doc);
	em.flush();
  }
  public Document getDocument(Document doc){
	  doc=em.find(Document.class, doc.getId());
	  em.refresh(doc);
	  return doc;
  }
  public Document createDocument(String title) {
	Document emp1 = new Document();
    emp1.setTitle(title);
    StringBuilder sb=new StringBuilder(15000);
    for (int i = 0; i < 5000; i++) {
		sb.append("Munish Chandel IS here to serve UBS. Sapient");
	}
    emp1.setContent(sb.toString());
    em.persist(emp1);
    Document emp = new Document();
    emp.setTitle(title);
//    emp.setRefDoc(emp1);
    em.persist(emp);
    
    Document emp2 = new Document();
    emp2.setTitle(title);
//    emp2.setRefDoc(emp1);
    Collection<Document> docList=new ArrayList<Document>();
    docList.add(emp);
    docList.add(emp1);
    
    emp2.setRelatedDocs(docList);
    em.persist(emp2);
    return emp;
  }

  public Collection<Document> findAllDocuments() {
    Query query = em.createQuery("SELECT e FROM Document e");
    query.setHint("toplink.refresh", "true");
    return (Collection<Document>) query.getResultList();
  }
public void saveAttachment(Attachment doc) {
	em.persist(doc);
}
public void deleteAttachment(Attachment attch) {
	attch=em.find(Attachment.class, attch.getId());
	em.remove(attch);
	em.flush();
}
public void deleteDocument(Document attch) {
	attch=em.find(Document.class, attch.getId());
	em.remove(attch);
	em.flush();
}
}