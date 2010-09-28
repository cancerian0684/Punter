/*
 * CreatePlayersAndTeams.java
 * 
 *
 * Copyright 2007 Sun Microsystems, Inc. ALL RIGHTS RESERVED Use of 
 * this software is authorized pursuant to the terms of the license 
 * found at http://developers.sun.com/berkeley_license.html.
 *
 */

package com.ubs.punter.jpa;

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

/**
 *
 * @author John O'Conner
 */
public class StaticDaoFacade {
	static{
		try{
		final NetworkServerControl serverControl = new NetworkServerControl(InetAddress.getByName("localhost"), 1527);
		serverControl.start(null);
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
    
    /**
     * @param args the command line arguments
     * @throws Exception 
     * @throws UnknownHostException 
     */
    public static void main(String[] args) throws UnknownHostException, Exception {
        // Create the EntityManager
        EntityManager em = emf.createEntityManager();
        
        em.getTransaction().begin();
        
       /* for(Team team: teams) {
            em.persist(team);
        }
        
        for(Player player: dodgersPlayers) {
            player.setTeam(teams[0]);
            teams[0].addPlayer(player);
            em.persist(player);
        }
        
        for (Player player: giantsPlayers) {
            player.setTeam(teams[1]);
            teams[1].addPlayer(player);
            em.persist(player);
        }*/
        Process p=new Process();
        p.setDescription("A test process");
        List<Task> taskList=new ArrayList<Task>();
        for(int i=0;i<=1;i++){
        Task t=new Task();
        t.setName("EchoTask_"+i);
        t.setProcess(p);
        Properties props = new Properties();
        props.setProperty("name", "munish chandel");
        t.setInputParams(props);
        taskList.add(t);
        }
        p.setTaskList(taskList);
        em.persist(p);
        em.getTransaction().commit();
        
        System.err.println("Listing tasks for process : "+p.getId()+"  -- "+p.getDescription());
        Process np = em.find(Process.class,p.getId());
        System.err.println("Listing tasks for process : "+np.getId()+"  -- "+np.getDescription());
        Collection<Task> tl = np.getTaskList();
        for (Task task : tl) {
			System.out.println(task.getName());
			System.out.println(task.getInputParams().getProperty("name"));
		}
        
//        listProcesses();
        em.close();
    }
    public static void main2(String[] args) throws UnknownHostException, Exception {
//    	getProcessTasksById(801L);
    	/*Task t=new Task();
        t.setName("EchoTask_113");
        Process p=new Process();
        p.setId(51L);
        t.setProcess(p);
        Properties props = new Properties();
        props.setProperty("name", "munish");
        props.setProperty("last", "chandel");
        t.setInputParams(props);
    	createTask(t);*/
    	listTask(351L);
//    	EntityManager em = emf.createEntityManager();
    	Task task = em.find(Task.class, 351L);
    	task.getInputParams().setProperty("*name", "Rohit Banyal");    	
    	saveTask(task);
    	task.getInputParams().setProperty("age", "29");
    	saveTask(task);
    	task.getInputParams().setProperty("dob", "05-June-1983");
    	saveTask(task);
//    	em.close();
    	listTask(351L);
    	removeTask(task);
	}
    public static void removeTask(Task task)throws Exception{
    	EntityManager em = emf.createEntityManager();
    	try{
    	em.getTransaction().begin();
    	Task tmp=em.find(Task.class, task.getId());
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
    public static void removeProcess(Process proc)throws Exception{
    	EntityManager em = emf.createEntityManager();
    	try{
    	em.getTransaction().begin();
    	Process tmp=em.find(Process.class, proc.getId());
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
    
    public static Task createTask(Task task)throws Exception{
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
    public static Process createProcess(Process proc)throws Exception{
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
    public static void saveTask(Task t)throws Exception{
        em.getTransaction().begin();
        Task task=em.find(Task.class, t.getId());
        task.setInputParams(t.getInputParams());
        task.setOutputParams(t.getOutputParams());
        em.merge(task);
        em.getTransaction().commit();
    }
    public static void saveProcess(Process p)throws Exception{
//    	EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Process task=em.find(Process.class, p.getId());
        task.setName(p.getName());
        em.merge(task);
        em.flush();
        em.getTransaction().commit();
//        em.close();
    }
 public static void listTask(long id)throws Exception{
        Task task=em.find(Task.class, id);
        try{
        if(task!=null){
        	System.out.println("Listing task for "+task.getId());
        	Set<Object> keySet = task.getInputParams().keySet();
        for (Object object : keySet) {
        	System.out.println(object.toString()+" -- "+task.getInputParams().get(object));
		}
        task.getOutputParams();
        }
        }catch(Exception e){
        	
        }
    }
    public static List<Process> getProcessList() throws Exception{
        Query q = em.createQuery("select p from Process p");
        q.setHint("toplink.refresh", "true");
        List<Process> processList = q.getResultList();
        return processList;
    }
    public static Process getProcess(long id) throws Exception{
    	EntityManager em = emf.createEntityManager();
    	try{
    		Process proc= em.find(Process.class, id);
    		em.refresh(proc);
    		return proc;
    	}finally{
    		em.close();
    	}
    }
    public static List<ProcessHistory> getProcessHistoryListForProcessId(long id) throws Exception{
    	EntityManager em = emf.createEntityManager();
    	try{
    		Process proc= em.find(Process.class, id);
    		em.refresh(proc);
    		List<ProcessHistory> phl = proc.getProcessHistoryList();
    		return phl;
    	}finally{
    		em.close();
    	}
    }
    public static List<Task> getProcessTasksById(long pid) throws UnknownHostException, Exception{
    	EntityManager em = emf.createEntityManager();
    	try{
        Process np = em.find(Process.class,pid);
        em.refresh(np);
    	List<Task> tl = np.getTaskList();
//    	System.err.println("Listing Tasks for process.");
    	for (Task task : tl) {
    		System.out.println(task.getName());
    	}
        return tl;
    	}finally{
    		em.close();
    	}
    }
    
    public static List<Task> getProcessTasks(long pid) throws UnknownHostException, Exception{
    	EntityManager em = emf.createEntityManager();
    	try{
    	Query q = em.createQuery("select p from Process p where p.id=:pid");
        q.setParameter("pid", pid);
        q.setHint("toplink.refresh", "true");
        List<Process> processList = q.getResultList();
    	System.out.println(processList.get(0).getDescription());
    	List<Task> tl = processList.get(0).getTaskList();
    	System.err.println("Listing Tasks for process.");
    	for (Task task : tl) {
    		System.out.println(task.getName());
    	}
        return tl;
    	}finally{
    		em.close();
    	}
    }
    private static void listProcesses(){
        Query q = em.createQuery("select p from Process p");
        List<Process> processList = q.getResultList();
        
        for (Process np : processList) {	
        	System.out.println(np.getDescription());
        	Collection<Task> tl = np.getTaskList();
        	for (Task task : tl) {
        		System.out.println(task.getName());
        	}
		}
    }
    private static Player[] dodgersPlayers = new Player[] {
        
        new Player("Lowe", "Derek", 23, "You just can't touch that sinker."),
        new Player("Kent", "Jeff", 12, "I'm getting too old for this."),
        new Player("Garciaparra", "Nomar", 5,
                "No, I'm not superstitious at all.")
                
    };
    
    private static Player[] giantsPlayers = new Player[] {
        new Player("Pettitte", "Andy", 46, null),
        new Player("Jeter", "Derek", 2, null),
        new Player("Rodriguez", "Alex", 13, null)
        
    };
    
    public static Team[] teams = new Team[] {
        new Team("Los Angeles Dodgers", "National"),
        new Team("San Francisco Giants", "National"),
        new Team("Anaheim Angels", "American"),
        new Team("Boston Red Sox", "American")
    };
}
