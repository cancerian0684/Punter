package com.ubs.punter.test;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ubs.punter.jpa.Process;
import com.ubs.punter.jpa.StaticDaoFacade;
import com.ubs.punter.jpa.Task;

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
		com.ubs.punter.jpa.Process process=new com.ubs.punter.jpa.Process();
		process.setId(1L);
		for(int i=0;i<=10;i++){
		Task task=new Task();
		task.setProcess(process);
		task.setName("Test Task");
		task=StaticDaoFacade.createTask(task);
		System.out.println(task.getId());
		StaticDaoFacade.removeTask(task);
		StaticDaoFacade.listTask(task.getId());
		}
	}

	@Test
	public void testCreateProcess() {
//		StaticDaoFacade.getProcess(id);
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSaveTask() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testListTask() throws Exception{
		com.ubs.punter.jpa.Process process=new com.ubs.punter.jpa.Process();
		process.setId(712L);
		List<Task> t1 = StaticDaoFacade.getProcessTasks(712L);
		Task task=new Task();
		task.setProcess(process);
		task.setName("Test Task");
		task=StaticDaoFacade.createTask(task);
		List<Task> t2 = StaticDaoFacade.getProcessTasks(712L);
		assertEquals(1, (t2.size()-t1.size()));
	}

	@Test
	public void testGetProcessList() throws Exception{
		com.ubs.punter.jpa.Process process=new com.ubs.punter.jpa.Process();
		process.setName("UBS-101");
		List<Process> pl1 = StaticDaoFacade.getProcessList();
		StaticDaoFacade.createProcess(process);
		List<Process> pl2 = StaticDaoFacade.getProcessList();
		assertEquals(1, (pl2.size()-pl1.size()));
	}

	@Test
	public void testGetProcess() throws Exception{
		List<Process> pl1 = StaticDaoFacade.getProcessList();
		for (Process process : pl1) {
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
