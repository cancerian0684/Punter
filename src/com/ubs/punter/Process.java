package com.ubs.punter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.sun.jmx.snmp.tasks.TaskServer;
import com.ubs.punter.gui.TaskObserver;
import com.ubs.punter.jpa.ProcessHistory;
import com.ubs.punter.jpa.Task;
import com.ubs.punter.jpa.TaskHistory;

public class Process {
private List<Tasks> taskList=new ArrayList<Tasks>();
private Map sessionMap=new HashMap<String, Object>();
private TaskObserver ts;
private ProcessHistory ph;
private Task taskDao;
public Process() {
	// TODO Auto-generated constructor stub
}
public void addTask(Tasks task,Task taskDao){
	this.taskDao=taskDao;
	task.setSessionMap(sessionMap);
	taskList.add(task);
}
public void execute(){
	int i=0;
	for (Tasks task : taskList) {
		boolean status=task.execute();
		i++;
		TaskHistory th=new TaskHistory();
		th.setProcessHistory(ph);
		th.setTask(taskDao);
		th.setSequence(i);
		th.setStatus(status);
		th.setLogs("dummy logs");
		ts.updateTaskHistory(th);
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
public void setTaskObservable(TaskObserver ts,ProcessHistory ph){
	this.ts=ts;
	this.ph=ph;
}
}
