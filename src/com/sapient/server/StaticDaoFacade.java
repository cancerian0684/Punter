package com.sapient.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.apache.derby.drda.NetworkServerControl;

import com.sapient.kb.jpa.Attachment;
import com.sapient.kb.jpa.Document;
import com.sapient.kb.utils.TestEditor;
import com.sapient.punter.jpa.ProcessData;
import com.sapient.punter.jpa.ProcessHistory;
import com.sapient.punter.jpa.TaskData;
import com.sapient.punter.jpa.TaskHistory;

public class StaticDaoFacade {
	private static StaticDaoFacade sdf;
	private EntityManagerFactory emf;
	public static StaticDaoFacade getInstance(){
		if(sdf==null){
			sdf=new StaticDaoFacade();
		}
		return sdf;
	}
	private StaticDaoFacade() {
		try{
			final NetworkServerControl serverControl = new NetworkServerControl(InetAddress.getByName("localhost"), 1527);
			try{
				serverControl.start(null);
				serverControl.logConnections(true);
				System.err.println(serverControl.getRuntimeInfo());
			}catch (Exception e) {
				e.printStackTrace();
			}
			Thread.currentThread();
			Runtime.getRuntime().addShutdownHook(new Thread(){
				@Override
				public void run() {
					try {
						emf.close();
						System.out.println("Shutting down DB server.");
					    serverControl.shutdown();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			Map properties = new HashMap();
//			properties.put(TopLinkProperties.CACHE_TYPE_DEFAULT, CacheType.DEFAULT);
			emf = Persistence.createEntityManagerFactory("punter",properties);
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
		/*EntityManager em = emf.createEntityManager();
	    DocumentService service = new DocumentService(em);
	    em.getTransaction().begin();
	    service.updateAccessCounter(doc);
	    em.getTransaction().commit();
	    em.close();*/
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
	  	try{
	  	DocumentService service = new DocumentService(em);
	    em.getTransaction().begin();
	    service.saveDocument(doc);
	    em.getTransaction().commit();
	    LuceneIndexDao.getInstance().indexDocs(doc);
	    return doc;
	  	}finally{
	  		em.close();	  		
	  	}
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
	  	EntityManager em = emf.createEntityManager();
	  	try{
	  	DocumentService service = new DocumentService(em);
	  	doc=service.getDocument(doc);
	  	}finally{
	    em.close();
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
public synchronized boolean deleteDocument(Document attch) {
	EntityManager em = emf.createEntityManager();
	em.getTransaction().begin();
  	DocumentService service = new DocumentService(em);
  	service.deleteDocument(attch);
  	em.getTransaction().commit();
    em.close();
    LuceneIndexDao.getInstance().deleteIndexForDoc(attch);
	return true;
}
public synchronized void rebuildIndex(){
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

public synchronized void removeTask(TaskData task)throws Exception{
	EntityManager em = emf.createEntityManager();
	try{
	em.getTransaction().begin();
	TaskData tmp=em.find(TaskData.class, task.getId());
    em.remove(tmp);
    em.flush();
    em.getTransaction().commit();
	}catch (Exception e) {
		e.printStackTrace();
		em.getTransaction().rollback();
	}finally{
		em.close();
	}
}
public synchronized void removeProcess(ProcessData proc)throws Exception{
	EntityManager em = emf.createEntityManager();
	try{
	em.getTransaction().begin();
	ProcessData tmp=em.find(ProcessData.class, proc.getId());
    em.remove(tmp);
    em.flush();
    em.getTransaction().commit();
	}catch (Exception e) {
		e.printStackTrace();
		em.getTransaction().rollback();
	}finally{
		em.close();
	}
}

public synchronized TaskData createTask(TaskData task)throws Exception{
	EntityManager em = emf.createEntityManager();
	try{
        em.getTransaction().begin();
        em.persist(task);
        em.flush();
        em.getTransaction().commit();
        return task;
	}finally{
		em.close();
	}
}
public synchronized ProcessData createProcess(ProcessData proc)throws Exception{
	EntityManager em = emf.createEntityManager();
	try{
    em.getTransaction().begin();
    em.persist(proc);
    em.flush();
    em.getTransaction().commit();
    return proc;
	}finally{
		em.close();
	}
}
public synchronized ProcessHistory createProcessHistory(ProcessHistory ph)throws Exception{
	EntityManager em = emf.createEntityManager();
	try{
    em.getTransaction().begin();
    em.persist(ph);
    em.flush();
    em.getTransaction().commit();
    return ph;
	}finally{
		em.close();
	}
}
public synchronized TaskHistory createTaskHistory(TaskHistory th)throws Exception{
	EntityManager em = emf.createEntityManager();
	try{
        em.getTransaction().begin();
        em.persist(th);
        em.flush();
        em.getTransaction().commit();
        return th;
	}finally{
		em.close();
	}
}
public synchronized void saveTaskHistory(TaskHistory t)throws Exception{
	EntityManager em = emf.createEntityManager();
    em.getTransaction().begin();
    TaskHistory task=em.find(TaskHistory.class, t.getId());
    task.setRunState(t.getRunState());
    task.setRunStatus(t.getRunStatus());
    task.setSequence(t.getSequence());
    task.setLogs(t.getLogs());
    em.merge(task);
    em.flush();
    em.getTransaction().commit();
    em.close();
}

	public synchronized void saveProcessHistory(ProcessHistory procHistory)
			throws Exception {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		ProcessHistory ph = em.find(ProcessHistory.class, procHistory.getId());
		ph.setRunState(procHistory.getRunState());
		ph.setRunStatus(procHistory.getRunStatus());
		ph.setFinishTime(procHistory.getFinishTime());
		em.merge(ph);
		em.flush();
		em.getTransaction().commit();
		em.close();
	}

	public synchronized TaskData saveTask(TaskData t) throws Exception {
		EntityManager em = emf.createEntityManager();
		try {
			em.getTransaction().begin();
			t=em.merge(t);
			em.lock(t, LockModeType.READ);
			em.flush();
			em.getTransaction().commit();
			return t;
		} catch (Exception e) {
			em.getTransaction().rollback();
			throw e;
		}finally{
			em.close();
		}
	}

	public synchronized ProcessData saveProcess(ProcessData p) throws Exception {
		EntityManager em = emf.createEntityManager();
		try {
			em.getTransaction().begin();
			p=em.merge(p);
			em.lock(p, LockModeType.READ);
			em.getTransaction().commit();
			return p;
		} catch (Exception e) {
			em.getTransaction().rollback();
			throw e;
		}finally{
			em.close();
		}
	}

	public synchronized void listTask(long id) throws Exception {
		EntityManager em = emf.createEntityManager();
		TaskData task = em.find(TaskData.class, id);
		try {
			if (task != null) {
				System.out.println("Listing task for " + task.getId());
				Set<String> keySet = task.getInputParams().keySet();
				for (String object : keySet) {
					System.out.println(object.toString() + " -- "
							+ task.getInputParams().get(object));
				}
				task.getOutputParams();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			em.close();
		}
	}

	public synchronized List<ProcessData> getScheduledProcessList()
			throws Exception {
		EntityManager em = emf.createEntityManager();
		try {
			Query q = em.createQuery("select p from ProcessData p");
			q.setHint("toplink.refresh", "true");
			List<ProcessData> dbProcList = q.getResultList();
			List<ProcessData> processList = new ArrayList<ProcessData>();
			for (ProcessData processDao : dbProcList) {
				String ss = processDao.getInputParams().get("scheduleString")
						.getValue().trim();
				if (!ss.isEmpty())
					processList.add(processDao);
			}
			return processList;
		} finally {
			em.close();
		}
	}

	public synchronized List<ProcessData> getProcessList() throws Exception {
		EntityManager em = emf.createEntityManager();
		try {
			Query q = em.createQuery("select p from ProcessData p");
			q.setHint("toplink.refresh", "true");
			List<ProcessData> processList = q.getResultList();
			return processList;
		} finally {
			em.close();
		}
	}
public synchronized ProcessData getProcess(long id) throws Exception{
	EntityManager em = emf.createEntityManager();
	try{
		ProcessData proc= em.find(ProcessData.class, id);
		em.refresh(proc);
		return proc;
	}finally{
		em.close();
	}
}
public synchronized TaskHistory getTaskDao(TaskHistory td) throws Exception{
	EntityManager em = emf.createEntityManager();
	try{
		TaskHistory proc= em.find(TaskHistory.class, td.getId());
		em.refresh(proc);
		return proc;
	}finally{
		em.close();
	}
}
public synchronized List<ProcessHistory> getProcessHistoryListForProcessId(long id) throws Exception{
	EntityManager em = emf.createEntityManager();
	try{
		ProcessData proc= em.find(ProcessData.class, id);
		em.refresh(proc);
		List<ProcessHistory> phl = proc.getProcessHistoryList();
		return phl;
	}finally{
		em.close();
	}
}
public synchronized List<ProcessHistory> getSortedProcessHistoryListForProcessId(long id) throws Exception{
	EntityManager em = emf.createEntityManager();
	try{
	Query q = em.createQuery("select ph from ProcessHistory ph where ph.process.id = :pid order by ph.id desc");
    q.setHint("toplink.refresh", "true");
    q.setParameter("pid", id);
    List<ProcessHistory> processHistoryList = q.getResultList();
    return processHistoryList;
	}finally{
		em.close();
	}
}
public synchronized ProcessHistory getProcessHistoryById(long id) throws Exception{
	EntityManager em = emf.createEntityManager();
	try{
		ProcessHistory proc= em.find(ProcessHistory.class, id);
		em.refresh(proc);
		return proc;
	}finally{
		em.close();
	}
}
public synchronized List<TaskData> getProcessTasksById(long pid) throws UnknownHostException, Exception{
	EntityManager em = emf.createEntityManager();
	try{
    ProcessData np = em.find(ProcessData.class, pid);
    em.refresh(np);
	List<TaskData> tl = np.getTaskList();
	for (TaskData task : tl) {
		System.out.println(task.getName());
	}
    return tl;
	}finally{
		em.close();
	}
}
public synchronized List<TaskData> getSortedTasksByProcessId(long pid) throws UnknownHostException, Exception{
	EntityManager em = emf.createEntityManager();
	try{
	Query q = em.createQuery("select t from TaskData t where t.process.id=:pid and t.active=true order by t.sequence");
    q.setParameter("pid", pid);
    q.setHint("toplink.refresh", "true");
    List<TaskData> taskList = q.getResultList();
	System.err.println("Listing Tasks for process.");
	for (TaskData task : taskList) {
		System.out.println(task.getSequence()+" -- "+task.getName());
	}
    return taskList;
	}finally{
		em.close();
	}
}
public synchronized List<TaskData> getProcessTasks(long pid) throws UnknownHostException, Exception{
	EntityManager em = emf.createEntityManager();
	try{
	Query q = em.createQuery("select p from ProcessData p where p.id=:pid");
    q.setParameter("pid", pid);
    q.setHint("toplink.refresh", "true");
    List<ProcessData> processList = q.getResultList();
	System.out.println(processList.get(0).getDescription());
	List<TaskData> tl = processList.get(0).getTaskList();
	System.err.println("Listing Tasks for process.");
	for (TaskData task : tl) {
		System.out.println(task.getName());
	}
    return tl;
	}finally{
		em.close();
	}
}
public synchronized void listProcesses(){
	EntityManager em = emf.createEntityManager();
	try{
    Query q = em.createQuery("select p from ProcessData p");
    List<ProcessData> processList = q.getResultList();
    
    for (ProcessData np : processList) {	
    	System.out.println(np.getDescription());
    	Collection<TaskData> tl = np.getTaskList();
    	for (TaskData task : tl) {
    		System.out.println(task.getName());
    	}
	}
	}finally{
		em.close();
	}
}

public synchronized void deleteTeam(){
    String aTeamName = "Anaheim Angels";
    
    // Create the EntityManager
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("league");
    EntityManager em = emf.createEntityManager();
    
    Query q = em.createQuery("delete from Team t " +
            "where t.teamName = :name");
    q.setParameter("name", aTeamName);
    
    em.getTransaction().begin();
    q.executeUpdate();
    em.getTransaction().commit();

    em.close();
    emf.close();
}
}