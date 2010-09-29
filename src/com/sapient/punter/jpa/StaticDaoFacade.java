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
        ProcessDao p=new ProcessDao();
        p.setDescription("A test process");
        List<TaskDao> taskList=new ArrayList<TaskDao>();
        for(int i=0;i<=1;i++){
        TaskDao t=new TaskDao();
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
        ProcessDao np = em.find(ProcessDao.class,p.getId());
        System.err.println("Listing tasks for process : "+np.getId()+"  -- "+np.getDescription());
        Collection<TaskDao> tl = np.getTaskList();
        for (TaskDao task : tl) {
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
        ProcessDao p=new ProcessDao();
        p.setId(51L);
        t.setProcess(p);
        Properties props = new Properties();
        props.setProperty("name", "munish");
        props.setProperty("last", "chandel");
        t.setInputParams(props);
    	createTask(t);*/
    	listTask(351L);
//    	EntityManager em = emf.createEntityManager();
    	TaskDao task = em.find(TaskDao.class, 351L);
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
    public static void removeTask(TaskDao task)throws Exception{
    	EntityManager em = emf.createEntityManager();
    	try{
    	em.getTransaction().begin();
    	TaskDao tmp=em.find(TaskDao.class, task.getId());
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
    public static void removeProcess(ProcessDao proc)throws Exception{
    	EntityManager em = emf.createEntityManager();
    	try{
    	em.getTransaction().begin();
    	ProcessDao tmp=em.find(ProcessDao.class, proc.getId());
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
    
    public static TaskDao createTask(TaskDao task)throws Exception{
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
    public static ProcessDao createProcess(ProcessDao proc)throws Exception{
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
    public static void saveTask(TaskDao t)throws Exception{
        em.getTransaction().begin();
        TaskDao task=em.find(TaskDao.class, t.getId());
        task.setInputParams(t.getInputParams());
        task.setOutputParams(t.getOutputParams());
        task.setSequence(t.getSequence());
        task.setDescription(t.getDescription());
        em.merge(task);
        em.getTransaction().commit();
    }
    public static void saveProcess(ProcessDao p)throws Exception{
//    	EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        ProcessDao proc=em.find(ProcessDao.class, p.getId());
        proc.setName(p.getName());
        proc.setInputParams(p.getInputParams());
        em.merge(proc);
        em.flush();
        em.getTransaction().commit();
//        em.close();
    }
 public static void listTask(long id)throws Exception{
        TaskDao task=em.find(TaskDao.class, id);
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
    public static List<ProcessDao> getProcessList() throws Exception{
        Query q = em.createQuery("select p from ProcessDao p");
        q.setHint("toplink.refresh", "true");
        List<ProcessDao> processList = q.getResultList();
        return processList;
    }
    public static ProcessDao getProcess(long id) throws Exception{
    	EntityManager em = emf.createEntityManager();
    	try{
    		ProcessDao proc= em.find(ProcessDao.class, id);
    		em.refresh(proc);
    		return proc;
    	}finally{
    		em.close();
    	}
    }
    public static List<ProcessHistory> getProcessHistoryListForProcessId(long id) throws Exception{
    	EntityManager em = emf.createEntityManager();
    	try{
    		ProcessDao proc= em.find(ProcessDao.class, id);
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
    public static List<TaskDao> getProcessTasksById(long pid) throws UnknownHostException, Exception{
    	EntityManager em = emf.createEntityManager();
    	try{
        ProcessDao np = em.find(ProcessDao.class,pid);
        em.refresh(np);
    	List<TaskDao> tl = np.getTaskList();
//    	System.err.println("Listing Tasks for process.");
    	for (TaskDao task : tl) {
    		System.out.println(task.getName());
    	}
        return tl;
    	}finally{
    		em.close();
    	}
    }
    public static List<TaskDao> getSortedTasksByProcessId(long pid) throws UnknownHostException, Exception{
    	EntityManager em = emf.createEntityManager();
    	try{
    	Query q = em.createQuery("select t from TaskDao t where t.process.id=:pid order by t.sequence");
        q.setParameter("pid", pid);
        q.setHint("toplink.refresh", "true");
        List<TaskDao> taskList = q.getResultList();
    	System.err.println("Listing Tasks for process.");
    	for (TaskDao task : taskList) {
    		System.out.println(task.getSequence()+" -- "+task.getName());
    	}
        return taskList;
    	}finally{
    		em.close();
    	}
    }
    public static List<TaskDao> getProcessTasks(long pid) throws UnknownHostException, Exception{
    	EntityManager em = emf.createEntityManager();
    	try{
    	Query q = em.createQuery("select p from ProcessDao p where p.id=:pid");
        q.setParameter("pid", pid);
        q.setHint("toplink.refresh", "true");
        List<ProcessDao> processList = q.getResultList();
    	System.out.println(processList.get(0).getDescription());
    	List<TaskDao> tl = processList.get(0).getTaskList();
    	System.err.println("Listing Tasks for process.");
    	for (TaskDao task : tl) {
    		System.out.println(task.getName());
    	}
        return tl;
    	}finally{
    		em.close();
    	}
    }
    private static void listProcesses(){
        Query q = em.createQuery("select p from ProcessDao p");
        List<ProcessDao> processList = q.getResultList();
        
        for (ProcessDao np : processList) {	
        	System.out.println(np.getDescription());
        	Collection<TaskDao> tl = np.getTaskList();
        	for (TaskDao task : tl) {
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
    public void findPlayer(){

        // Create the EntityManager
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("league");
        EntityManager em = emf.createEntityManager();
        
        
        for(long primaryKey = 1; primaryKey < 10; primaryKey++) {
            Player player = em.find(Player.class, primaryKey);
            if (player != null) {
                System.out.println(player.toString());
            }
            
        }
        
        em.close();
        emf.close();
        // TODO code application logic here
    
    }
    public void mergePlayer(){

        
        // Create the EntityManager
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("league");
        EntityManager em = emf.createEntityManager();
        
        Player p = em.find(Player.class, 1L);
        em.clear();
        // p is now detached
        Team t = new Team("Ventura Surfers", "National");
        p.setTeam(t);
        em.getTransaction().begin();
        Player managedPlayer = em.merge(p);
        em.getTransaction().commit();
        System.out.println(p.toString());
        
        
        em.close();
        emf.close();
        
    }
    public void removePlayer(){

        // Create the EntityManager
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("league");
        EntityManager em = emf.createEntityManager();
        
        em.getTransaction().begin();
        Player player = em.find(Player.class, 5L);
        if (player != null) {
            System.out.println(player.toString());
            em.remove(player);
        }
        
        em.getTransaction().commit();
        
        em.close();
        emf.close();
    
    }
    public void retrievePlayer(){

        String aTeamName = "Los Angeles Dodgers";
        
        // Create the EntityManager
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("league");
        EntityManager em = emf.createEntityManager();
        
        Query q = em.createQuery("select c from Player c where c.team.teamName = :name");
        q.setParameter("name", aTeamName);
        List<Player> playerList = q.getResultList();

        for(Player p : playerList) {
            System.out.println(p.toString());
        }
        
        em.close();
        emf.close();
         
    
    }
    public void updatePlayer(){

        String aTeamName = "Los Angeles Dodgers";

		// Create the EntityManager
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("league");
		EntityManager em = emf.createEntityManager();
		
		Query q = em.createQuery("update Player p " +
		        "set p.jerseyNumber = (p.jerseyNumber + 1) " +
		        "where p.team.teamName = :name");
		q.setParameter("name", aTeamName);
		
		em.getTransaction().begin();
		q.executeUpdate();
		em.getTransaction().commit();
		
		em.close();
		emf.close();

    }
}
