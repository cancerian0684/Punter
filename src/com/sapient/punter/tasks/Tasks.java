package com.sapient.punter.tasks;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.MemoryHandler;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;

import com.sapient.punter.annotations.InputParam;
import com.sapient.punter.annotations.OutputParam;
import com.sapient.punter.jpa.TaskData;
import com.sapient.punter.utils.InputParamValue;
import com.sapient.punter.utils.OutputParamValue;

public abstract class Tasks implements Serializable{
	private Map<String,Object> sessionMap;
	private Map<String,OutputParamValue> outputParams;
	private HashMap<String, InputParamValue> inputParams;
	protected TaskData taskDao;
	private transient ConsoleHandler cHandler = null;
	private transient MemoryHandler mHandler = null;
	private transient Level loggingLevel=Level.INFO;
	private StringBuilder strLogger;
	private Document logDocument;
	public static final ThreadLocal<Logger> LOGGER = new ThreadLocal<Logger>() {
		@Override
		protected Logger initialValue() {
		    Logger logger = Logger.getLogger("Logger for "
			    + Thread.currentThread().getName());
		    System.out.println("Created logger: " + logger.getName());
		    return logger;
		}
	};
	public void beforeTaskStart(){
		strLogger = new StringBuilder();
	   /* cHandler = new ConsoleHandler();
	    cHandler.setFormatter(new Formatter() {
			@Override
			public String format(LogRecord record) {
				return new Date(record.getMillis())+" ["+Thread.currentThread().getName()+"] "+record.getLevel()
      		  + " "+record.getSourceClassName()+"." 
	          + record.getSourceMethodName() + "() - "
	          + record.getMessage() + "\r\n";
			}
		});*/
	    mHandler = new MemoryHandler(new Handler() {
	        public void publish(LogRecord record) {
	        	String msg=new Date(record.getMillis())+" ["+Thread.currentThread().getName()+"] "+record.getLevel()
      		  + " "+record.getSourceClassName()+"." 
	          + record.getSourceMethodName() + "() - "
	          + record.getMessage();
	        	strLogger.append(msg+"\r");
	        	try {
					logDocument.insertString(logDocument.getLength (),record.getMessage()+"\n", null);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
	    }

        public void flush() {
        	System.out.println("flush called..");
        }

        public void close() {
        }
        
      }, 2, loggingLevel);
	    LOGGER.get().addHandler(mHandler);
//	    LOGGER.get().addHandler(cHandler);
	    LOGGER.get().setUseParentHandlers(false);
	}
	public void setLoggingLevel(Level loggingLevel) {
		this.loggingLevel = loggingLevel;
	}
	public String getMemoryLogs(){
		if(strLogger!=null)
		return strLogger.toString();
		return "";
	}
	public void setTaskDao(TaskData taskDao) {
		this.taskDao = taskDao;
	}
	public TaskData getTaskDao() {
		return taskDao;
	}
	public static HashMap<String,InputParamValue> listInputParams(Tasks task){
		Field[] fields = task.getClass().getDeclaredFields();
		System.out.println("Listing input params");
		HashMap<String,InputParamValue> inProp=new HashMap<String,InputParamValue>();
		for (Field field : fields) {
			if(field.isAnnotationPresent(InputParam.class)){
				InputParam ann = field.getAnnotation(InputParam.class);
				System.out.println(ann.required()==true?"*"+field.getName():""+field.getName());
				inProp.put(field.getName(), new InputParamValue(ann, ""));
			}
		}
		return inProp;
	}
	
	public static HashMap<String,OutputParamValue> listOutputParams(Tasks task){
		Field[] fields = task.getClass().getDeclaredFields();
		System.out.println("Listing output params");
		HashMap<String,OutputParamValue> outProp=new HashMap<String, OutputParamValue>();
		for (Field field : fields) {
			if(field.isAnnotationPresent(OutputParam.class)){
				OutputParam ann = field.getAnnotation(OutputParam.class);
				System.out.println(field.getName());
				outProp.put(field.getName(), new OutputParamValue(ann, ""));
			}
		}
		return outProp;
	}
	public static Tasks getTask(String taskName, HashMap<String, InputParamValue> input, HashMap<String,OutputParamValue> outputParams){
		try {
			Class<?> clz=Class.forName(taskName);
			Tasks task=(Tasks) clz.newInstance();
			task.setOutputParams(outputParams);
			task.setInputParams(input);
			return task;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void substituteParams() throws Exception{
		Field[] fields = getClass().getDeclaredFields();
		for (Field field : fields) {
			if(field.isAnnotationPresent(InputParam.class)){
				try {
					field.setAccessible(true);
					String fieldValue=getInputParams().get(field.getName()).getValue();
					if(fieldValue.length()>=1){
						if(fieldValue.startsWith("$")){
							fieldValue=fieldValue.substring(1);
							fieldValue=(String) getSessionObject(fieldValue);
						}
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
//					e.printStackTrace();
//					LOGGER.get().severe(e.toString());
					throw e;
				} catch (IllegalAccessException e) {
//					e.printStackTrace();
//					LOGGER.get().severe(e.toString());
					throw e;
				} catch (ParseException e) {
//					LOGGER.get().log(Level.SEVERE,e.toString(),e);
					throw e;
				}
			}
		}
	}
	public void substituteResult(){
		Field[] fields = getClass().getDeclaredFields();
		for (Field field : fields) {
			if(field.isAnnotationPresent(OutputParam.class)){
				try {
					field.setAccessible(true);
					Object value = field.get(this);
					sessionMap.put(outputParams.get(field.getName()).getValue(), value);
					System.out.println(field.getName()+" bound to "+outputParams.get(field.getName()).getValue()+" == "+value);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public void afterTaskFinish(){
		LOGGER.get().removeHandler(mHandler);
		LOGGER.get().removeHandler(cHandler);
	}
	/*
	 * @return returns the status of the task. true means success and false is failure
	 */
	public abstract boolean run();
	
	public boolean execute()throws Exception{
		substituteParams();
		boolean status= run();
		substituteResult();
		return status;
	}
	public void setOutputParams(HashMap<String,OutputParamValue> outputParams) {
		this.outputParams = outputParams;
	}
	public Map<String,OutputParamValue> getOutputParams() {
		return outputParams;
	}
	
	public Map<String,InputParamValue> getInputParams() {
		return inputParams;
	}

	public void setInputParams(HashMap<String,InputParamValue> inputParams) {
		this.inputParams = inputParams;
	}

	public void setSessionMap(Map sessionMap) {
		this.sessionMap = sessionMap;
	}
	public void setLogDocument(Document logDocument) {
		this.logDocument = logDocument;
	}
	public Object getSessionObject(String key) {
		return sessionMap.get(key);
	}

	public void setSessionObject(String key, Object obj) {
		sessionMap.put(key, obj);
	}
}

