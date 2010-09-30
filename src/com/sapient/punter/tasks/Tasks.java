package com.sapient.punter.tasks;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.MemoryHandler;

import com.sapient.punter.annotations.InputParam;
import com.sapient.punter.annotations.OutputParam;
import com.sapient.punter.jpa.TaskDao;

public abstract class Tasks {
	private Map<String,Object> sessionMap;
	private Map<String,String> outputParams;
	private Properties inputParams;
	protected TaskDao taskDao;
	private ConsoleHandler handler = null;
	private MemoryHandler mHandler = null;
	private Level loggingLevel=Level.INFO;
	private StringBuilder strLogger;
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
		System.err.println("Calling before Task: ");
		strLogger = new StringBuilder();
	    handler = new ConsoleHandler();
	    handler.setFormatter(new Formatter() {
			@Override
			public String format(LogRecord record) {
				return new Date(record.getMillis())+" ["+Thread.currentThread().getName()+"] "+record.getLevel()
      		  + " "+record.getSourceClassName()+"." 
	          + record.getSourceMethodName() + "() - "
	          + record.getMessage() + "\r\n";
			}
		});
	    mHandler = new MemoryHandler(new Handler() {
	        public void publish(LogRecord record) {
	        	strLogger.append(new Date(record.getMillis())+" ["+Thread.currentThread().getName()+"] "+record.getLevel()
	        		  + " "+record.getSourceClassName()+"." 
			          + record.getSourceMethodName() + "() - "
			          + record.getMessage() + "\r");
	    }

        public void flush() {
        	System.out.println("flush called..");
        }

        public void close() {
        }
        
      }, 2, loggingLevel);
	    LOGGER.get().addHandler(mHandler);
	    LOGGER.get().addHandler(handler);
	    LOGGER.get().setUseParentHandlers(false);
	}
	public void setLoggingLevel(Level loggingLevel) {
		this.loggingLevel = loggingLevel;
	}
	public String getMemoryLogs(){
		return strLogger.toString();
	}
	public void setTaskDao(TaskDao taskDao) {
		this.taskDao = taskDao;
	}
	public TaskDao getTaskDao() {
		return taskDao;
	}
	public static List<String> listInputParams(Tasks task){
		Field[] fields = task.getClass().getDeclaredFields();
		System.out.println("Listing input params");
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
	
	public static List<String> listOutputParams(Tasks task){
		Field[] fields = task.getClass().getDeclaredFields();
		System.out.println("Listing output params");
		List<String> outParams=new ArrayList<String>(10);
		for (Field field : fields) {
			if(field.isAnnotationPresent(OutputParam.class)){
				OutputParam ann = field.getAnnotation(OutputParam.class);
				System.out.println(field.getName());
				outParams.add(field.getName());
			}
		}
		return outParams;
	}
	public static Tasks getTask(String taskName,Properties input, Properties outputParams){
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

	private void substituteParams() {
		Field[] fields = getClass().getDeclaredFields();
		for (Field field : fields) {
			if(field.isAnnotationPresent(InputParam.class)){
				try {
					field.setAccessible(true);
					String fieldValue=getInputParams().getProperty(field.getName(),"");
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
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
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
					sessionMap.put(outputParams.get(field.getName()), value);
					System.out.println(field.getName()+" bound to "+outputParams.get(field.getName())+" == "+value);
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
		LOGGER.get().removeHandler(handler);
	}
	/*
	 * @return returns the status of the task. true means success and false is failure
	 */
	public abstract boolean run();
	
	public boolean execute(){
		System.out.println("started executing task..");
		substituteParams();
		beforeTaskStart();
		boolean status= run();
		substituteResult();
		afterTaskFinish();
		return status;
	}
	public void setOutputParams(Map outputParams) {
		this.outputParams = outputParams;
	}
	public Map<String,String> getOutputParams() {
		return outputParams;
	}
	
	public Properties getInputParams() {
		return inputParams;
	}

	public void setInputParams(Properties inputParams) {
		this.inputParams = inputParams;
	}

	public void setSessionMap(Map sessionMap) {
		this.sessionMap = sessionMap;
	}

	public Object getSessionObject(String key) {
		return sessionMap.get(key);
	}

	public void setSessionObject(String key, Object obj) {
		sessionMap.put(key, obj);
	}
}

