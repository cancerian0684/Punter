package com.sapient.punter.tasks;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;

import com.sapient.kb.jpa.StaticDaoFacade;
import com.sapient.punter.annotations.InputParam;
import com.sapient.punter.gui.ProcessObserver;
import com.sapient.punter.gui.TaskObserver;
import com.sapient.punter.jpa.ProcessHistory;
import com.sapient.punter.jpa.RunState;
import com.sapient.punter.jpa.RunStatus;
import com.sapient.punter.jpa.TaskHistory;
import com.sapient.punter.utils.InputParamValue;
import com.sapient.punter.utils.StringUtils;

public class Process implements Serializable{
private List<Tasks> taskList=new ArrayList<Tasks>();
private Map sessionMap=new HashMap<String, Object>();
private transient TaskObserver ts;
private boolean failed=false;
private HashMap<String, InputParamValue> inputParams;
private ProcessHistory ph;
@InputParam(description="Stop process on task failure")
private boolean stopOnTaskFailure=true;
@InputParam(description="Always raise alert for the task")
private boolean alwaysRaiseAlert=false;
@InputParam
private String comments;
@InputParam(description="Any of ALL, INFO, WARNING, SEVERE")
private String loggingLevel;
@InputParam(description="Comma separated email List")
private String emailsToNotify;
@InputParam
private boolean emailsOnFailureOnly;
@InputParam(description="<html>provide cron4j formatted scheduling string<br>13 * * jan-jun,sep-dec mon-fri,sat")
private String scheduleString;
protected transient ProcessObserver po;
private int lineBufferSize=1000;
private Document logDocument;
public Document getLogDocument() {
	return logDocument;
}
public Process() {
}
public void addObserver(ProcessObserver po){
	this.po=po;
}

public void beforeProcessStart(){
	logDocument=new PlainDocument(){
		protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
			super.insertUpdate(chng, attr);
			Element root = getDefaultRootElement();
			while (root.getElementCount() > lineBufferSize)
			{
				Element firstLine = root.getElement(0);
				try
				{
//					System.err.println("removing");
					remove(0, firstLine.getEndOffset());
				}
				catch(BadLocationException ble)
				{
					System.out.println(ble+" = "+lineBufferSize);
				}
			}
		};
	};
//	System.err.println("Emails to notify : "+emailsToNotify);
	ph.setRunState(RunState.RUNNING);
	po.update(ph);
}
public void afterProcessFinish(){
	try{
		StaticDaoFacade.getInstance().saveProcessHistory(ph);
	}catch (Exception e) {
		e.printStackTrace();
	}
	po.update(ph);
	po.processCompleted();
}
public void execute(){
	substituteParams();
	beforeProcessStart();
	boolean keepRunning=true;
	int progressCounter=0;
	ph.setRunStatus(RunStatus.RUNNING);
	ph.setLogDocument(logDocument);
	for (TaskHistory th : ph.getTaskHistoryList()) {
		Tasks task=Tasks.getTask(th.getTask().getClassName(), th.getTask().getInputParams(), th.getTask().getOutputParams());
		task.setTaskDao(th.getTask());
		task.setSessionMap(sessionMap);
		task.setLogDocument(logDocument);
		th.setRunState(RunState.RUNNING);
		boolean status=false;
		if(keepRunning){
		try{
			task.beforeTaskStart();
			task.LOGGER.get().log(Level.INFO,"started executing task.."+task.getTaskDao().getSequence()+" - "+task.getTaskDao().getName());
			status=task.execute();
			task.LOGGER.get().log(Level.INFO,"Finished executing task.."+task.getTaskDao().getSequence()+" - "+task.getTaskDao().getName());
			if(stopOnTaskFailure){
				keepRunning=status;
			}
			if(!status){
				failed=true;
			}
		}catch (Throwable e) {
			failed=true;
			if(stopOnTaskFailure){
				keepRunning=false;
			}
			task.LOGGER.get().log(Level.SEVERE, StringUtils.getExceptionStackTrace(e));
			task.LOGGER.get().log(Level.INFO,"Finished executing task.."+task.getTaskDao().getSequence()+" - "+task.getTaskDao().getName());
		}finally{
			task.afterTaskFinish();
		}
		progressCounter++;
		ph.setProgress(100*progressCounter/ph.getTaskHistoryList().size());
		po.update(ph);
		th.setStatus(status);
		if(status)
			th.setRunStatus(RunStatus.SUCCESS);
		else
			th.setRunStatus(RunStatus.FAILURE);
		
			th.setRunState(RunState.COMPLETED);
			th.setLogs(task.getMemoryLogs());
			ts.saveTaskHistory(th);
		}else{
			th.setRunState(RunState.NOT_RUN);
			th.setRunStatus(RunStatus.NOT_RUN);
			th.setLogs("");
			ts.saveTaskHistory(th);
		}
	}
	ph.setRunState(RunState.COMPLETED);
	if(!failed){
		ph.setRunStatus(RunStatus.SUCCESS);
		if(!alwaysRaiseAlert)
			ph.setClearAlert(true);
	}
	else{
		ph.setRunStatus(RunStatus.FAILURE);
	}
		
	afterProcessFinish();
}
public void setTaskObservable(TaskObserver ts){
	this.ts=ts;
}

public static HashMap<String,InputParamValue> listInputParams(){
	Field[] fields = Process.class.getDeclaredFields();
	System.out.println("Listing input params for process");
	HashMap<String,InputParamValue> inProp=new HashMap<String,InputParamValue>();
	for (Field field : fields) {
		if(field.isAnnotationPresent(InputParam.class)){
			InputParam ann = field.getAnnotation(InputParam.class);
//			System.out.println(ann.required()==true?"*"+field.getName():""+field.getName());
			inProp.put(field.getName(), new InputParamValue(ann, ""));
		}
	}
	return inProp;
}
private void substituteParams() {
	Field[] fields = getClass().getDeclaredFields();
	for (Field field : fields) {
		if(field.isAnnotationPresent(InputParam.class)){
			try {
				field.setAccessible(true);
				String fieldValue=inputParams.get(field.getName()).getValue();
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
					}else if(field.getType().getSimpleName().equals("boolean")){
						boolean tmp=Boolean.parseBoolean(fieldValue);
						field.set(this,tmp);
				}
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				Tasks.LOGGER.get().severe(e.toString());
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				Tasks.LOGGER.get().severe(e.toString());
			} catch (ParseException e) {
				e.printStackTrace();
				Tasks.LOGGER.get().severe(e.toString());
			} catch (Exception e) {
				e.printStackTrace();
				Tasks.LOGGER.get().severe(e.toString());
			}
		}
	}
}
public static Process getProcess(HashMap<String, InputParamValue> props,ProcessHistory ph){
	Process proc=new Process();
	proc.inputParams=props;
	proc.ph=ph;
	return proc;
}
public static void main(String[] args) {
	listInputParams();
}
}
