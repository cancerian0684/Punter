package com.sapient.punter.tasks;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;

import com.sapient.kb.jpa.StaticDaoFacade;
import com.sapient.punter.annotations.InputParam;
import com.sapient.punter.gui.AppSettings;
import com.sapient.punter.gui.ProcessObserver;
import com.sapient.punter.gui.TaskObserver;
import com.sapient.punter.jpa.ProcessHistory;
import com.sapient.punter.jpa.RunState;
import com.sapient.punter.jpa.RunStatus;
import com.sapient.punter.jpa.TaskHistory;
import com.sapient.punter.utils.EmailService;
import com.sapient.punter.utils.InputParamValue;
import com.sapient.punter.utils.StringUtils;

public class Process implements Serializable{
	private List<Tasks> taskList = new ArrayList<Tasks>();
	private Map sessionMap = new HashMap<String, Object>();
	private transient TaskObserver ts;
	private boolean failed = false;
	private HashMap<String, InputParamValue> inputParams;
	private ProcessHistory ph;
	@InputParam(description = "Stop process on task failure")
	private boolean stopOnTaskFailure = true;
	@InputParam(description = "Always raise alert for the task")
	private boolean alwaysRaiseAlert = false;
	@InputParam
	private String comments;
	@InputParam(description = "Any of FINE, INFO, WARNING, SEVERE")
	private String loggingLevel;
	@InputParam(description = "Comma separated email List")
	private String emailsToNotify;
	@InputParam
	private boolean emailsOnFailureOnly;
	@InputParam
	private boolean doVariableSubstitution = true;
	@InputParam(description = "<html>provide cron4j formatted scheduling string<br>13 * * jan-jun,sep-dec mon-fri,sat")
	private String scheduleString;
	protected transient ProcessObserver po;
	private int lineBufferSize = 1000;
	private Document logDocument;
	private Logger processLogger;

	public Document getLogDocument() {
		return logDocument;
	}

	public Process() {
	}

	public void addObserver(ProcessObserver po) {
		this.po = po;
	}

	public void beforeProcessStart() {
		sessionMap.putAll(AppSettings.getInstance().getSessionMap());
		logDocument = new PlainDocument() {
			protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
				super.insertUpdate(chng, attr);
				Element root = getDefaultRootElement();
				while (root.getElementCount() > lineBufferSize) {
					Element firstLine = root.getElement(0);
					try {
						remove(0, firstLine.getEndOffset());
					} catch (BadLocationException ble) {
						System.out.println(ble + " = " + lineBufferSize);
					}
				}
			};
		};
		// System.err.println("Emails to notify : "+emailsToNotify);
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
	if(emailsToNotify!=null&&!emailsToNotify.isEmpty()){
		if(emailsOnFailureOnly&&ph.getRunStatus()==RunStatus.SUCCESS)
			return;
		try{
			if(processLogger!=null){
				processLogger.log(Level.INFO,"Sending Task Report Mail to : "+emailsToNotify);
			}
			StringBuilder processLogs=new StringBuilder(10000);
			for (TaskHistory th : ph.getTaskHistoryList()) {
				processLogs.append(th.getLogs()+"\r\n");
			}
			EmailService.getInstance().sendEMail("Punter Task : ["+ph.getRunStatus()+"] "+ph.getName(), emailsToNotify, processLogs.toString());
			if(processLogger!=null){
				processLogger.log(Level.INFO,"Mail Sent.");
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
private void setLoggingLevel(Logger processLogger){
	if(loggingLevel==null){
		return ;
	}
	if(loggingLevel.equalsIgnoreCase("info"))
		processLogger.setLevel(Level.INFO);
	else if(loggingLevel.equalsIgnoreCase("fine"))
		processLogger.setLevel(Level.FINE);
	else if(loggingLevel.equalsIgnoreCase("warning"))
		processLogger.setLevel(Level.WARNING);
	else if(loggingLevel.equalsIgnoreCase("severe"))
		processLogger.setLevel(Level.SEVERE);
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
		task.setDoVariableSubstitution(doVariableSubstitution);
		th.setRunState(RunState.RUNNING);
		th.setRunStatus(RunStatus.RUNNING);
		boolean status=false;
		if(keepRunning){
		try{
			task.beforeTaskStart();
			processLogger=task.LOGGER.get();
			setLoggingLevel(processLogger);
			processLogger.log(Level.FINE,"started executing task.."+task.getTaskDao().getSequence()+" - "+task.getTaskDao().getName());
			status=task.execute();
			processLogger.log(Level.FINE,"Finished executing task.."+task.getTaskDao().getSequence()+" - "+task.getTaskDao().getName());
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
			task.LOGGER.get().log(Level.FINE,"Finished executing task.."+task.getTaskDao().getSequence()+" - "+task.getTaskDao().getName());
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
		if(field.isAnnotationPresent(InputParam.class) && (inputParams.get(field.getName())!=null)){
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
