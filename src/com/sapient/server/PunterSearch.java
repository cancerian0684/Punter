package com.sapient.server;

import java.rmi.Remote;
import java.util.List;

import com.sapient.kb.jpa.Attachment;
import com.sapient.kb.jpa.Document;

public interface PunterSearch extends Remote {
	 void updateAccessCounter(Document doc);
	 Document createDocument();
	 List<Document> getDocList(String q,String category,boolean isSpclTxt,boolean isAND);
	 Document saveDocument(Document doc);
	 Attachment saveAttachment(Attachment attach);
	 Document getDocument(Document doc);
	 boolean deleteAttachment(Attachment attch);
	 boolean deleteDocument(Document attch);
	 void rebuildIndex();
}
