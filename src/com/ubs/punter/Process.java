package com.ubs.punter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Process {
private List<Tasks> taskList=new ArrayList<Tasks>();
private Map sessionMap=new HashMap<String, Object>();

public Process() {
	// TODO Auto-generated constructor stub
}
public void addTask(Tasks task){
	task.setSessionMap(sessionMap);
	taskList.add(task);
}
public void execute(){
	for (Tasks task : taskList) {
		task.execute();
	}
}
}
