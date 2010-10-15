/*
 * CreatePlayersAndTeams.java
 * 
 *
 * Copyright 2007 Sun Microsystems, Inc. ALL RIGHTS RESERVED Use of 
 * this software is authorized pursuant to the terms of the license 
 * found at http://developers.sun.com/berkeley_license.html.
 *
 */

package com.sapient.punter.jpa;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;
import javax.persistence.Query;

import oracle.toplink.essentials.config.CacheType;
import oracle.toplink.essentials.config.TopLinkProperties;

import org.apache.derby.drda.NetworkServerControl;

import com.sapient.kb.jpa.Document;
import com.sapient.kb.jpa.DocumentService;

/**
 *
 * @author John O'Conner
 */
public class StaticDaoFacade {
	static{
		try{
//		final NetworkServerControl serverControl = new NetworkServerControl(InetAddress.getByName("localhost"), 1527);
//		serverControl.start(null);
		Thread.currentThread();
		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run() {
				 try {
					 em.close();
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
	static Map properties = new HashMap();
	{
		properties.put(TopLinkProperties.CACHE_TYPE_DEFAULT, CacheType.Full);
	}
    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("punter",properties);
    private static EntityManager em = emf.createEntityManager();
    {
    	em.setFlushMode(FlushModeType.COMMIT);
    }
    /** Creates a new instance of CreatePlayersAndTeams */
    public StaticDaoFacade() {
    }
    
    public static void removeTask(TaskData task)throws Exception{
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
    public static void removeProcess(ProcessData proc)throws Exception{
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
    
    public static TaskData createTask(TaskData task)throws Exception{
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
    public static ProcessData createProcess(ProcessData proc)throws Exception{
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
    public static ProcessHistory createProcessHistory(ProcessHistory ph)throws Exception{
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
    public static TaskHistory createTaskHistory(TaskHistory th)throws Exception{
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
    public static void saveTaskHistory(TaskHistory t)throws Exception{
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
    public static void saveProcessHistory(ProcessHistory procHistory)throws Exception{
    	EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        ProcessHistory ph=em.find(ProcessHistory.class, procHistory.getId());
        ph.setRunState(procHistory.getRunState());
        ph.setRunStatus(procHistory.getRunStatus());
        ph.setFinishTime(procHistory.getFinishTime());
        em.merge(ph);
        em.flush();
        em.getTransaction().commit();
        em.close();
    }
    public static void saveTask(TaskData t)throws Exception{
        em.getTransaction().begin();
        TaskData task=em.find(TaskData.class, t.getId());
        task.setInputParams(t.getInputParams());
        task.setOutputParams(t.getOutputParams());
        task.setSequence(t.getSequence());
        task.setDescription(t.getDescription());
        task.setActive(t.isActive());
        em.merge(task);
        em.getTransaction().commit();
    }
    public static void saveProcess(ProcessData p)throws Exception{
//    	EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        ProcessData proc=em.find(ProcessData.class, p.getId());
        proc.setName(p.getName());
        proc.setInputParams(p.getInputParams());
        em.merge(proc);
        em.flush();
        em.getTransaction().commit();
//        em.close();
    }
 public static void listTask(long id)throws Exception{
        TaskData task=em.find(TaskData.class, id);
        try{
        if(task!=null){
        	System.out.println("Listing task for "+task.getId());
        	Set<String> keySet = task.getInputParams().keySet();
        for (String object : keySet) {
        	System.out.println(object.toString()+" -- "+task.getInputParams().get(object));
		}
        task.getOutputParams();
        }
        }catch(Exception e){
        	
        }
    }
 	public static List<ProcessData> getScheduledProcessList() throws Exception{
	     Query q = em.createQuery("select p from ProcessData p");
	     q.setHint("toplink.refresh", "true");
	     List<ProcessData> dbProcList = q.getResultList();
	     List<ProcessData> processList =new ArrayList<ProcessData>();
	     for (ProcessData processDao :dbProcList  ) {
	    	 String ss=processDao.getInputParams().get("scheduleString").getValue().trim();
	    	 System.out.println(ss);
	    	 if(!ss.isEmpty())
	    	 processList.add(processDao);
		}
	     return processList;
 	}
    public static List<ProcessData> getProcessList() throws Exception{
        Query q = em.createQuery("select p from ProcessData p");
        q.setHint("toplink.refresh", "true");
        List<ProcessData> processList = q.getResultList();
        return processList;
    }
    public static ProcessData getProcess(long id) throws Exception{
    	EntityManager em = emf.createEntityManager();
    	try{
    		ProcessData proc= em.find(ProcessData.class, id);
    		em.refresh(proc);
    		return proc;
    	}finally{
    		em.close();
    	}
    }
    public static TaskHistory getTaskDao(TaskHistory td) throws Exception{
    	EntityManager em = emf.createEntityManager();
    	try{
    		TaskHistory proc= em.find(TaskHistory.class, td.getId());
    		em.refresh(proc);
    		return proc;
    	}finally{
    		em.close();
    	}
    }
    public static List<ProcessHistory> getProcessHistoryListForProcessId(long id) throws Exception{
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
    public static List<ProcessHistory> getSortedProcessHistoryListForProcessId(long id) throws Exception{
        Query q = em.createQuery("select ph from ProcessHistory ph where ph.process.id = :pid order by ph.id desc");
        q.setHint("toplink.refresh", "true");
        q.setParameter("pid", id);
        List<ProcessHistory> processHistoryList = q.getResultList();
        return processHistoryList;
    }
    public static ProcessHistory getProcessHistoryById(long id) throws Exception{
    	EntityManager em = emf.createEntityManager();
    	try{
    		ProcessHistory proc= em.find(ProcessHistory.class, id);
    		em.refresh(proc);
    		return proc;
    	}finally{
    		em.close();
    	}
    }
    public static List<TaskData> getProcessTasksById(long pid) throws UnknownHostException, Exception{
    	EntityManager em = emf.createEntityManager();
    	try{
        ProcessData np = em.find(ProcessData.class,pid);
        em.refresh(np);
    	List<TaskData> tl = np.getTaskList();
//    	System.err.println("Listing Tasks for process.");
    	for (TaskData task : tl) {
    		System.out.println(task.getName());
    	}
        return tl;
    	}finally{
    		em.close();
    	}
    }
    public static List<TaskData> getSortedTasksByProcessId(long pid) throws UnknownHostException, Exception{
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
    public static List<TaskData> getProcessTasks(long pid) throws UnknownHostException, Exception{
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
    private static void listProcesses(){
        Query q = em.createQuery("select p from ProcessData p");
        List<ProcessData> processList = q.getResultList();
        
        for (ProcessData np : processList) {	
        	System.out.println(np.getDescription());
        	Collection<TaskData> tl = np.getTaskList();
        	for (TaskData task : tl) {
        		System.out.println(task.getName());
        	}
		}
    }
    
    public void deleteTeam(){
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
