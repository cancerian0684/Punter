package com.shunya.punter.test;

import com.shunya.kb.jpa.StaticDaoFacade;
import com.shunya.punter.gui.AppSettings;
import com.shunya.punter.jpa.ProcessData;
import com.shunya.punter.jpa.ProcessHistory;
import com.shunya.punter.jpa.TaskData;
import com.shunya.punter.jpa.TaskHistory;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class StaticDaoFacadeTestCase {
	@Test
	public void testRemoveTask() throws Exception{
		com.shunya.punter.jpa.ProcessData process=new com.shunya.punter.jpa.ProcessData();
		process.setId(1L);
		for(int i=0;i<=10;i++){
		TaskData task=new TaskData();
		task.setProcess(process);
		task.setName("Test Task");
		task=StaticDaoFacade.getInstance().createTask(task);
		System.out.println(task.getId());
		StaticDaoFacade.getInstance().removeTask(task);
		StaticDaoFacade.getInstance().listTask(task.getId());
		}
	}

	@Test
	public void testCreateProcessHistory() throws Exception {
		ProcessData proc=new ProcessData();
		proc.setId(1603L);
		ProcessHistory ph=new ProcessHistory();
		ph.setName("Test-1");
		ph.setStartTime(new Date());
		ph.setProcess(proc);
		StaticDaoFacade.getInstance().createProcessHistory(ph);
		TaskData task=new TaskData();
		task.setId(1602L);
		for(int i=0;i<=10;i++){
		TaskHistory th=new TaskHistory();
		th.setProcessHistory(ph);
		th.setTask(task);
		th.setSequence(1);
		th.setStatus(true);
		th.setLogs("dummy logs");
		StaticDaoFacade.getInstance().createTaskHistory(th);
		
		}
	}

	@Test
	public void testGetProcessHistoryForProcessId() throws Exception{
		List<ProcessHistory> phl = StaticDaoFacade.getInstance().getProcessHistoryListForProcessId(1603L);
		for (ProcessHistory processHistory : phl) {
			System.out.println(processHistory.getId());
		}
	}
	@Test
	public void testGetProcessTaskHistoryForProcessHistoryId() throws Exception{
		List<ProcessHistory> phl = StaticDaoFacade.getInstance().getProcessHistoryListForProcessId(1603L);
		for (ProcessHistory processHistory : phl) {
			System.out.println(processHistory.getId());
		}
	}

	@Test
	public void testListTask() throws Exception{
		com.shunya.punter.jpa.ProcessData process=new com.shunya.punter.jpa.ProcessData();
		process.setId(712L);
		List<TaskData> t1 = StaticDaoFacade.getInstance().getProcessTasks(712L);
		TaskData task=new TaskData();
		task.setProcess(process);
		task.setName("Test Task");
		task=StaticDaoFacade.getInstance().createTask(task);
		List<TaskData> t2 = StaticDaoFacade.getInstance().getProcessTasks(712L);
		assertEquals(1, (t2.size()-t1.size()));
	}

	@Test
	public void testGetProcessList() throws Exception{
		com.shunya.punter.jpa.ProcessData process=new com.shunya.punter.jpa.ProcessData();
		process.setName("UBS-101");
		List<ProcessData> pl1 = StaticDaoFacade.getInstance().getProcessList(AppSettings.getInstance().getUsername());
		StaticDaoFacade.getInstance().createProcess(process);
		List<ProcessData> pl2 = StaticDaoFacade.getInstance().getProcessList(AppSettings.getInstance().getUsername());
		assertEquals(1, (pl2.size()-pl1.size()));
	}

	@Test
	public void testGetProcess() throws Exception{
		List<ProcessData> pl1 = StaticDaoFacade.getInstance().getProcessList(AppSettings.getInstance().getUsername());
		for (ProcessData process : pl1) {
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
