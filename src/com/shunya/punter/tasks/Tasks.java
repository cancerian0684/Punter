package com.shunya.punter.tasks;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.MemoryHandler;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.xml.bind.JAXBException;

import com.shunya.punter.annotations.InputParam;
import com.shunya.punter.annotations.OutputParam;
import com.shunya.punter.jpa.TaskData;
import com.shunya.punter.utils.FieldProperties;
import com.shunya.punter.utils.FieldPropertiesMap;

public abstract class Tasks implements Serializable {
    private Map<String, Object> sessionMap;
    private FieldPropertiesMap outputParams;
    private FieldPropertiesMap inputParams;
    protected TaskData taskDao;
    private transient ConsoleHandler cHandler = null;
    private transient MemoryHandler mHandler = null;
    private transient Level loggingLevel = Level.FINE;
    private StringBuilder strLogger;
    private Document logDocument;
    private boolean doVariableSubstitution = false;

    public void setDoVariableSubstitution(boolean doVariableSubstitution) {
        this.doVariableSubstitution = doVariableSubstitution;
    }

    public static final ThreadLocal<Logger> LOGGER = new ThreadLocal<Logger>() {
        @Override
        protected Logger initialValue() {
            Logger logger = Logger.getLogger("Logger for "
                    + Thread.currentThread().getName());
//		    System.out.println("Created logger: " + logger.getName());
            return logger;
        }
    };

    public void beforeTaskStart() {
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
                String msg = new Date(record.getMillis()) + " [" + record.getLevel() + "] "
                        + record.getMessage();
                strLogger.append(msg + "\r");
                try {
                    logDocument.insertString(logDocument.getLength(), record.getMessage() + "\n", null);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }

            public void flush() {
//        	System.out.println("flush called..");
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

    public String getMemoryLogs() {
        if (strLogger != null)
            return strLogger.toString();
        return "";
    }

    public void setTaskDao(TaskData taskDao) {
        this.taskDao = taskDao;
    }

    public TaskData getTaskDao() {
        return taskDao;
    }

    public static FieldPropertiesMap listInputParams(Tasks task) {
        Field[] fields = task.getClass().getDeclaredFields();
        Map<String, FieldProperties> fieldPropertiesMap = new HashMap<String, FieldProperties>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(InputParam.class)) {
                InputParam ann = field.getAnnotation(InputParam.class);
//				System.out.println(ann.required()==true?"*"+field.getName():""+field.getName());
                fieldPropertiesMap.put(field.getName(), new FieldProperties(field.getName(), "", ann.description(), ann.required()));
            }
        }
        return new FieldPropertiesMap(fieldPropertiesMap);
    }

    public static FieldPropertiesMap listOutputParams(Tasks task) {
        Field[] fields = task.getClass().getDeclaredFields();
//		System.out.println("Listing output params");
        Map<String, FieldProperties> fieldPropertiesMap = new HashMap<String, FieldProperties>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(OutputParam.class)) {
                OutputParam ann = field.getAnnotation(OutputParam.class);
                fieldPropertiesMap.put(field.getName(), new FieldProperties(field.getName(), "", "", false));
            }
        }
        return new FieldPropertiesMap(fieldPropertiesMap);
    }

    public static Tasks getTask(String taskName, FieldPropertiesMap input, FieldPropertiesMap output) {
        try {
            Class<?> clz = Class.forName(taskName);
            Tasks task = (Tasks) clz.newInstance();
            task.setOutputParams(output);
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

    private void substituteParams() throws Exception {
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(InputParam.class)) {
                try {
                    field.setAccessible(true);
                    if (getInputParams().get(field.getName()) != null) {
                        String fieldValue = getInputParams().get(field.getName()).getValue();
                        if (fieldValue.length() >= 1) {
                            if (fieldValue.startsWith("$")) {
                                fieldValue = fieldValue.substring(1);
                                fieldValue = (String) getSessionObject(fieldValue);
                            }
                            if (doVariableSubstitution && fieldValue.contains("#{")) {
                                fieldValue = substituteVariables(fieldValue, sessionMap);
                            }
                            if (field.getType().getSimpleName().equals("String")) {
                                field.set(this, fieldValue);
                            } else if (field.getType().getSimpleName().equals("int")) {
                                int tmp = Integer.parseInt(fieldValue);
                                field.set(this, tmp);
                            } else if (field.getType().getSimpleName().equals("Date")) {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
                                field.set(this, sdf.parse(fieldValue));
                            } else if (field.getType().getSimpleName().equals("double")) {
                                double tmp = Double.parseDouble(fieldValue);
                                field.set(this, tmp);
                            } else if (field.getType().getSimpleName().equals("boolean")) {
                                boolean tmp = Boolean.parseBoolean(fieldValue);
                                field.set(this, tmp);
                            }

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

    public void substituteResult() {
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(OutputParam.class)) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(this);
                    sessionMap.put(outputParams.get(field.getName()).getValue(), value);
//					System.out.println(field.getName()+" bound to "+outputParams.get(field.getName()).getValue()+" == "+value);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void loadSessionVariables(Map<String, String> variableMap) {
        sessionMap.putAll(variableMap);
    }

    public void afterTaskFinish() {
        LOGGER.get().removeHandler(mHandler);
        LOGGER.get().removeHandler(cHandler);
    }

    /*
      * @return returns the status of the task. true means success and false is failure
      */
    public abstract boolean run();

    public boolean execute() throws Exception {
        substituteParams();
        boolean status = run();
        substituteResult();
        return status;
    }

    private String substituteVariables(String inputString, Map<String, Object> variables) {
        List<String> vars = getVariablesFromString(inputString);
        for (String string : vars) {
            String variable = "#{" + string + "}";
            if (null == variables.get(string)) {
                throw new RuntimeException("Variable Binding not Found :" + variable + " TaskName : " + taskDao.getName());
            }
            String variableBinding = variables.get(string).toString();
            inputString = inputString.replace(variable, variableBinding);
        }
        return inputString;
    }

    private static List<String> getVariablesFromString(String test) {
        char prevChar = ' ';
        String var = "";
        List<String> vars = new ArrayList<String>();
        boolean found = false;
        for (int i = 0; i < test.length(); i++) {
            char ch = test.charAt(i);
            if (ch == '{' && prevChar == '#') {
                var = "";
                found = true;
            } else if (ch == '}') {
                found = false;
                if (!var.isEmpty())
                    vars.add(var);
                var = "";
            } else if (found) {
                var += ch;
            }
            prevChar = ch;
        }
        return vars;
    }

    public void setOutputParams(FieldPropertiesMap outputParams) {
        this.outputParams = outputParams;
    }

    public FieldPropertiesMap getOutputParams() {
        return outputParams;
    }

    public FieldPropertiesMap getInputParams() {
        return inputParams;
    }

    public void setInputParams(FieldPropertiesMap inputParams) {
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

