package com.sapient.punter.test;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sapient.punter.jpa.ProcessDao;
import com.sapient.punter.jpa.ProcessHistory;
import com.sapient.punter.jpa.StaticDaoFacade;
import com.sapient.punter.jpa.TaskDao;
import com.sapient.punter.jpa.TaskHistory;

public class StaticDaoFacadeTestCase {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRemoveTask() throws Exception{
		com.sapient.punter.jpa.ProcessDao process=new com.sapient.punter.jpa.ProcessDao();
		process.setId(1L);
		for(int i=0;i<=10;i++){
		TaskDao task=new TaskDao();
		task.setProcess(process);
		task.setName("Test Task");
		task=StaticDaoFacade.createTask(task);
		System.out.println(task.getId());
		StaticDaoFacade.removeTask(task);
		StaticDaoFacade.listTask(task.getId());
		}
	}

	@Test
	public void testCreateProcessHistory() throws Exception {
		ProcessDao proc=new ProcessDao();
		proc.setId(1603L);
		ProcessHistory ph=new ProcessHistory();
		ph.setName("Test-1");
		ph.setStartTime(new Date());
		ph.setProcess(proc);
		StaticDaoFacade.createProcessHistory(ph);
		TaskDao task=new TaskDao();
		task.setId(1602L);
		for(int i=0;i<=10;i++){
		TaskHistory th=new TaskHistory();
		th.setProcessHistory(ph);
		th.setTask(task);
		th.setSequence(1);
		th.setStatus(true);
		th.setLogs("dummy logs");
		StaticDaoFacade.createTaskHistory(th);
		
		}
	}

	@Test
	public void testGetProcessHistoryForProcessId() throws Exception{
		List<ProcessHistory> phl = StaticDaoFacade.getProcessHistoryListForProcessId(1603L);
		for (ProcessHistory processHistory : phl) {
			System.out.println(processHistory.getId());
		}
	}
	@Test
	public void testGetProcessTaskHistoryForProcessHistoryId() throws Exception{
		List<ProcessHistory> phl = StaticDaoFacade.getProcessHistoryListForProcessId(1603L);
		for (ProcessHistory processHistory : phl) {
			System.out.println(processHistory.getId());
		}
	}

	@Test
	public void testListTask() throws Exception{
		com.sapient.punter.jpa.ProcessDao process=new com.sapient.punter.jpa.ProcessDao();
		process.setId(712L);
		List<TaskDao> t1 = StaticDaoFacade.getProcessTasks(712L);
		TaskDao task=new TaskDao();
		task.setProcess(process);
		task.setName("Test Task");
		task=StaticDaoFacade.createTask(task);
		List<TaskDao> t2 = StaticDaoFacade.getProcessTasks(712L);
		assertEquals(1, (t2.size()-t1.size()));
	}

	@Test
	public void testGetProcessList() throws Exception{
		com.sapient.punter.jpa.ProcessDao process=new com.sapient.punter.jpa.ProcessDao();
		process.setName("UBS-101");
		List<ProcessDao> pl1 = StaticDaoFacade.getProcessList();
		StaticDaoFacade.createProcess(process);
		List<ProcessDao> pl2 = StaticDaoFacade.getProcessList();
		assertEquals(1, (pl2.size()-pl1.size()));
	}

	@Test
	public void testGetProcess() throws Exception{
		List<ProcessDao> pl1 = StaticDaoFacade.getProcessList();
		for (ProcessDao process : pl1) {
			System.out.println(process.getId()+" -- "+process.getName());
		}
	}

	@Test
	public void testGetProcessTasksById() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetProcessTasks() {
		fail("Not yet implemented"); // TODO
	}

}
