package com.shunya.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.xml.bind.JAXBException;

import com.shunya.kb.gui.SearchQuery;
import com.shunya.punter.tasks.*;
import com.shunya.punter.tasks.Process;
import com.shunya.punter.utils.FieldPropertiesMap;
import org.apache.derby.drda.NetworkServerControl;

import com.shunya.kb.jpa.Attachment;
import com.shunya.kb.jpa.Document;
import com.shunya.punter.jpa.ProcessData;
import com.shunya.punter.jpa.ProcessHistory;
import com.shunya.punter.jpa.TaskData;
import com.shunya.punter.jpa.TaskHistory;
import com.shunya.punter.utils.FieldProperties;

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
			Properties p = System.getProperties();
			p.put("derby.system.home", ".");
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
			emf = Persistence.createEntityManagerFactory("punter",properties);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	    
  public List<String> getCategories(){
	List<String> categories=new ArrayList<String>(20);
	Scanner scanner = new Scanner(StaticDaoFacade.class.getClassLoader().getResourceAsStream("resources/categories.properties"));
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
  public Document createDocument(String author){
	  	EntityManager em = emf.createEntityManager();
	    em.getTransaction().begin();
	    Document doc = new Document();
	    doc.setTitle("test title");
	    doc.setContent("".getBytes());
	    doc.setDateCreated(new Date());
	    doc.setDateUpdated(new Date());
	    doc.setCategory("/all");
	    doc.setAuthor(author);
	    em.persist(doc);
	    em.flush();
	    LuceneIndexDao.getInstance().indexDocs(doc);
	    em.getTransaction().commit();
	    em.close();
	    return doc;
  }
  public List<Document> getDocList(SearchQuery query){
	   long t1=System.currentTimeMillis();
	   List<Document> result = LuceneIndexDao.getInstance().search(query.getQuery(), query.getCategory(),query.isSpecialText(),query.isAndFilter(),0, query.getMaxResults());
	   long t2=System.currentTimeMillis();
	   System.err.println("time consumed : "+(t2-t1));
	   return result;
  }
  public Document saveDocument(Document doc){
	  	EntityManager em = emf.createEntityManager();
	  	try{
	    em.getTransaction().begin();
	    doc=em.merge(doc);
	    em.flush();
	    em.getTransaction().commit();
	    doc=em.find(Document.class, doc.getId());
	    em.refresh(doc);
	    LuceneIndexDao.getInstance().indexDocs(doc);
	    return doc;
	  	}finally{
	  		em.close();	  		
	  	}
  }
  public Attachment saveAttachment(Attachment attach){
	  	EntityManager em = emf.createEntityManager();
	    em.getTransaction().begin();
	    em.persist(attach);
	    em.flush();
	    Document doc=attach.getDocument();
	    em.getTransaction().commit();
	    doc=em.find(Document.class, doc.getId());
	    em.refresh(doc);
	    em.close();
	    LuceneIndexDao.getInstance().indexDocs(doc);
	    return attach;
}
  public Document getDocument(Document doc){
	  	EntityManager em = emf.createEntityManager();
	  	try{
	  		doc=em.find(Document.class, doc.getId());
	  		em.refresh(doc);
	  	}finally{
	  		em.close();
	  	}
	    return doc;
}
public boolean deleteAttachment(Attachment attch) {
	EntityManager em = emf.createEntityManager();
	em.getTransaction().begin();
  	attch=em.find(Attachment.class, attch.getId());
	em.remove(attch);
	em.flush();
  	Document doc=attch.getDocument();
  	em.getTransaction().commit();
  	doc=em.find(Document.class, doc.getId());
  	em.refresh(doc);
    em.close();
    LuceneIndexDao.getInstance().indexDocs(doc);
	return true;
}
public boolean deleteDocument(Document doc) {
	EntityManager em = emf.createEntityManager();
	em.getTransaction().begin();
	doc=em.find(Document.class, doc.getId());
	em.remove(doc);
	em.flush();
  	em.getTransaction().commit();
    em.close();
    LuceneIndexDao.getInstance().deleteIndexForDoc(doc);
	return true;
}
public  void rebuildIndex(){
	System.out.println("Clearing old index");
	LuceneIndexDao.getInstance().deleteIndex();
	EntityManager em = emf.createEntityManager();
	em.getTransaction().begin();
	Query query = em.createQuery("SELECT e FROM Document e");
	List<Document> allDocs = query.getResultList();
    for (Document emp : allDocs) {
      System.out.println(emp.getCategory());
      LuceneIndexDao.getInstance().indexDocs(emp);
    }
    em.close();
}

public  void removeTask(TaskData task)throws Exception{
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
public  void removeProcess(ProcessData proc)throws Exception{
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

public  TaskData createTask(TaskData task)throws Exception{
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
public  ProcessData createProcess(ProcessData proc)throws Exception{
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
public  ProcessHistory createProcessHistory(ProcessHistory ph)throws Exception{
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
public  TaskHistory createTaskHistory(TaskHistory th)throws Exception{
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
	public  void saveTaskHistory(TaskHistory t)throws Exception{
		System.out.println("-------------------------------START");
		EntityManager em = emf.createEntityManager();
	    em.getTransaction().begin();
	    TaskHistory taskHistory=em.find(TaskHistory.class, t.getId());
	    taskHistory.setRunState(t.getRunState());
	    taskHistory.setRunStatus(t.getRunStatus());
	    taskHistory.setSequence(t.getSequence());
	    taskHistory.setLogs(t.getLogs());
	    em.merge(taskHistory);
//	    em.flush();
	    em.getTransaction().commit();
	    em.close();
	    System.out.println("-------------------------------END");
	}

	public  void saveProcessHistory(ProcessHistory procHistory)
			throws Exception {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		ProcessHistory ph = em.find(ProcessHistory.class, procHistory.getId());
		ph.setRunState(procHistory.getRunState());
		ph.setRunStatus(procHistory.getRunStatus());
		ph.setFinishTime(procHistory.getFinishTime());
		ph.setClearAlert(procHistory.isClearAlert());
		em.merge(ph);
//		em.flush();
		em.getTransaction().commit();
		em.close();
	}

	public  TaskData saveTask(TaskData t) throws Exception {
		EntityManager em = emf.createEntityManager();
		try {
			em.getTransaction().begin();
			TaskData tmp = em.find(TaskData.class, t.getId());
//			em.lock(tmp, LockModeType.READ);
			tmp.setActive(t.isActive());
			tmp.setAuthor(t.getAuthor());
			tmp.setClassName(t.getClassName());
			tmp.setDescription(t.getDescription());
			tmp.setInputParams(t.getInputParams());
			tmp.setOutputParams(t.getOutputParams());
			tmp.setName(t.getName());
			tmp.setProcess(t.getProcess());
			tmp.setSequence(t.getSequence());
			em.merge(tmp);
			em.flush();
			em.getTransaction().commit();
			return tmp;
		} catch (Exception e) {
			em.getTransaction().rollback();
			throw e;
		}finally{
			em.close();
		}
	}

	public  ProcessData saveProcess(ProcessData p) throws Exception {
		EntityManager em = emf.createEntityManager();
		try {
			em.getTransaction().begin();
			ProcessData tmp = em.find(ProcessData.class, p.getId());
//			em.lock(tmp, LockModeType.READ);
			tmp.setName(p.getName());
			tmp.setInputParams(p.getInputParams());
			tmp.setUsername(p.getUsername());
			tmp.setDescription(p.getDescription());
			em.flush();
			em.getTransaction().commit();
			return p;
		} catch (Exception e) {
			em.getTransaction().rollback();
			throw e;
		}finally{
			em.close();
		}
	}

	public  void listTask(long id) throws Exception {
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

	public  List<ProcessData> getScheduledProcessList(String username)
			throws Exception {
		EntityManager em = emf.createEntityManager();
		try {
			Query q = em.createQuery("select p from ProcessData p where p.username=:username");
			q.setParameter("username", username);
			q.setHint("eclipselink.refresh", "true");
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

	public  List<ProcessData> getProcessList(String username) throws Exception {
		EntityManager em = emf.createEntityManager();
		try {
			Query q = em.createQuery("select p from ProcessData p where p.username=:username order by p.id asc");
			q.setParameter("username", username);
			q.setHint("eclipselink.refresh", "true");
			List<ProcessData> processList = q.getResultList();
			return processList;
		} finally {
			em.close();
		}
	}
	public void updateAllProcessProperties(){
		EntityManager em = emf.createEntityManager();
		try {
			Query q = em.createQuery("select p from ProcessData p");
			q.setHint("eclipselink.refresh", "true");
			List<ProcessData> processList = q.getResultList();
			for (ProcessData processData : processList) {
                FieldPropertiesMap inProp = Process.listInputParams();
				 processData.setInputParams(inProp);
				 try {
				 	em.getTransaction().begin();
					ProcessData tmp = em.find(ProcessData.class, processData.getId());
					tmp.setInputParams(processData.getInputParams());
					em.flush();
					em.getTransaction().commit();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (JAXBException e) {
            e.printStackTrace();
        } finally {
			em.close();
		}
	}
	public  ProcessData getProcess(long id) throws Exception{
		EntityManager em = emf.createEntityManager();
		try{
			ProcessData proc= em.find(ProcessData.class, id);
			em.refresh(proc);
			return proc;
		}finally{
			em.close();
		}
	}
	public  TaskHistory getTaskDao(TaskHistory td) throws Exception{
		EntityManager em = emf.createEntityManager();
		try{
			TaskHistory proc= em.find(TaskHistory.class, td.getId());
			em.refresh(proc);
			return proc;
		}finally{
			em.close();
		}
	}
	public  List<ProcessHistory> getProcessHistoryListForProcessId(long id) throws Exception{
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
	public  List<ProcessHistory> getSortedProcessHistoryListForProcessId(long id) throws Exception{
		EntityManager em = emf.createEntityManager();
		try{
		Query q = em.createQuery("select ph from ProcessHistory ph where ph.process.id = :pid order by ph.id desc");
	    q.setHint("eclipselink.refresh", "true");
	    q.setParameter("pid", id);
	    q.setFirstResult(0);
	    q.setMaxResults(ServerSettings.getInstance().getMaxProcessHistory());
	    List<ProcessHistory> processHistoryList = q.getResultList();
	    return processHistoryList;
		}finally{
			em.close();
		}
	}
	public  ProcessHistory getProcessHistoryById(long id) throws Exception{
	EntityManager em = emf.createEntityManager();
	try{
		ProcessHistory proc= em.find(ProcessHistory.class, id);
		em.refresh(proc);
		return proc;
	}finally{
		em.close();
	}
}
public  List<TaskData> getProcessTasksById(long pid) throws UnknownHostException, Exception{
	EntityManager em = emf.createEntityManager();
	try{
		Query q = em.createQuery("select t from TaskData t where t.process.id=:pid order by t.sequence");
	    q.setParameter("pid", pid);
	    q.setHint("eclipselink.refresh", "true");
	    List<TaskData> taskList = q.getResultList();
		System.err.println("Listing Tasks for process.");
		for (TaskData task : taskList) {
			System.out.println(task.getSequence()+" -- "+task.getName());
		}
	    return taskList==null? Collections.EMPTY_LIST:taskList;
	}finally{
		em.close();
	}
}
public  List<TaskData> getSortedTasksByProcessId(long pid) throws UnknownHostException, Exception{
	EntityManager em = emf.createEntityManager();
	try{
	Query q = em.createQuery("select t from TaskData t where t.process.id=:pid and t.active=true order by t.sequence");
    q.setParameter("pid", pid);
    q.setHint("eclipselink.refresh", "true");
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
public  List<TaskData> getProcessTasks(long pid) throws UnknownHostException, Exception{
	EntityManager em = emf.createEntityManager();
	try{
	Query q = em.createQuery("select p from ProcessData p where p.id=:pid");
    q.setParameter("pid", pid);
    q.setHint("eclipselink.refresh", "true");
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

public  void deleteTeam(){
    String aTeamName = "Anaheim Angels";
    
    // Create the EntityManager
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("league");
    EntityManager em = emf.createEntityManager();
    
    @SuppressWarnings({"JpaQlInspection"})
    Query q = em.createQuery("delete from Team t where t.teamName = :name");
    q.setParameter("name", aTeamName);
    
    em.getTransaction().begin();
    q.executeUpdate();
    em.getTransaction().commit();

    em.close();
    emf.close();
}

public void deleteStaleHistory(int days){
	EntityManager em = emf.createEntityManager();
	try{
	Query q = em.createQuery("select ph from ProcessHistory ph where ph.startTime <= :staledate");
	Calendar cal=GregorianCalendar.getInstance();
	cal.set(cal.DATE, cal.get(cal.DATE)-days);
	 q.setHint("eclipselink.refresh", "true");
    q.setParameter("staledate",cal,TemporalType.TIMESTAMP);
    q.setFirstResult(0);
    q.setMaxResults(100);
    List<ProcessHistory> processHistoryList = q.getResultList();
    em.getTransaction().begin();
    for (ProcessHistory processHistory : processHistoryList) {
	  ProcessHistory ph = em.find(ProcessHistory.class, processHistory.getId());
	  em.remove(ph);
	  System.out.println("Removed : "+ph.getId());
    }
    em.getTransaction().commit();
	}catch (Exception e) {
		e.printStackTrace();
	}
	finally{
		em.close();
	}
}

public List<ProcessHistory> getMySortedProcessHistoryList(String username) {
	EntityManager em = emf.createEntityManager();
	try{
	Query q = em.createQuery("select ph from ProcessHistory ph where ph.process.username = :username AND ph.clearAlert=false order by ph.startTime desc");
    q.setHint("eclipselink.refresh", "true");
    q.setParameter("username", username);
//    q.setParameter("runStatus", RunStatus.SUCCESS);
    q.setFirstResult(0);
    q.setMaxResults(ServerSettings.getInstance().getMaxProcessAlerts());
    List<ProcessHistory> processHistoryList = q.getResultList();
    return processHistoryList;
	}finally{
		em.close();
	}
}

public void compressTables(){ 
    String []tables={"PROCESSHISTORY","PROCESS","TASK","TASKHISTORY","DOCUMENT","DOCUMENT_LOB","ATTACHMENT","ATTACHMENT_LOB"};
    EntityManager em = emf.createEntityManager(); 
    try{ 
    for (String string : tables) { 
            Query q = em.createNativeQuery("call SYSCS_UTIL.SYSCS_COMPRESS_TABLE('PUNTER', '"+string+"', 1)"); 
            em.getTransaction().begin(); 
            q.executeUpdate(); 
            em.getTransaction().commit();           
    } 
    }catch (Exception e) { 
            e.printStackTrace(); 
    }finally{ 
            em.close(); 
    } 
} 
}