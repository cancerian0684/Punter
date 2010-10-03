package com.sapient.punter.tasks;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.sapient.punter.annotations.InputParam;
import com.sapient.punter.gui.ProcessObserver;
import com.sapient.punter.gui.TaskObserver;
import com.sapient.punter.jpa.ProcessHistory;
import com.sapient.punter.jpa.RunState;
import com.sapient.punter.jpa.TaskDao;
import com.sapient.punter.jpa.TaskHistory;
import com.sun.jmx.snmp.tasks.TaskServer;

public class Process {
private List<Tasks> taskList=new ArrayList<Tasks>();
private Map sessionMap=new HashMap<String, Object>();
private TaskObserver ts;
private Properties inputParams;
private ProcessHistory ph;
@InputParam
private String comments;
@InputParam
private String loggingLevel;
@InputParam
private String emailsToNotify;
@InputParam
private boolean emailsOnFailureOnly;
@InputParam
private String scheduleString;
protected ProcessObserver po;
public Process() {
}
public void addObserver(ProcessObserver po){
	this.po=po;
}

public void beforeProcessStart(){
	System.err.println("Emails to notify : "+emailsToNotify);
	ph.setRunState(RunState.RUNNING);
	try {
		for (int i = 0; i < 8; i++) {			
			TimeUnit.MILLISECONDS.sleep(20);
			po.update(ph);
		}
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
}
public void afterProcessFinish(){
	ph.setRunState(RunState.SUCCESS);
	po.update(ph);
	po.processCompleted();
}
public void execute(){
	substituteParams();
	beforeProcessStart();
	for (TaskHistory th : ph.getTaskHistoryList()) {
		Tasks task=Tasks.getTask(th.getTask().getClassName(), th.getTask().getInputParams(), th.getTask().getOutputParams());
		task.setTaskDao(th.getTask());
		task.setSessionMap(sessionMap);
		th.setRunState(RunState.RUNNING);
		boolean status=task.execute();
		th.setStatus(status);
		if(status)
			th.setRunState(RunState.SUCCESS);
		else
			th.setRunState(RunState.FAILURE);
		th.setLogs(task.getMemoryLogs());
		ts.saveTaskHistory(th);
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	afterProcessFinish();
}
public void setTaskObservable(TaskObserver ts){
	this.ts=ts;
}

public static List<String> listInputParams(){
	Field[] fields = Process.class.getDeclaredFields();
	System.out.println("Listing input params for process");
	List<String> inParams=new ArrayList<String>(10);
	for (Field field : fields) {
		if(field.isAnnotationPresent(InputParam.class)){
			InputParam ann = field.getAnnotation(InputParam.class);
			System.out.println(ann.required()==true?"*"+field.getName():""+field.getName());
			inParams.add(ann.required()==true?""+field.getName():""+field.getName());
		}
	}
	return inParams;
}
private void substituteParams() {
	Field[] fields = getClass().getDeclaredFields();
	for (Field field : fields) {
		if(field.isAnnotationPresent(InputParam.class)){
			try {
				field.setAccessible(true);
				String fieldValue=inputParams.getProperty(field.getName(),"");
				if(fieldValue.length()>=1){
					if(field.getType().getSimpleName().equals("String")){
						field.set(this, fieldValue);
					}else if(field.getType().getSimpleName().equals("int")){
						int tmp=Integer.parseInt(fieldValue);
						field.set(this,tmp);
					}else if(field.getType().getSimpleName().equals("Date")){
						SimpleDateFormat sdf=new SimpleDateFormat("dd-MMM-yyyy");
						field.set(this,sdf.parse(fieldValue));
					}else if(field.getType().getSimpleName().equals("double")){
						double tmp=Double.parseDouble(fieldValue);
						field.set(this,tmp);
					}
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}
}
public static Process getProcess(Properties props,ProcessHistory ph){
	Process proc=new Process();
	proc.inputParams=props;
	proc.ph=ph;
	return proc;
}
public static void main(String[] args) {
	listInputParams();
}
}
