package org.shunya.punter.tasks;

import org.shunya.punter.annotations.InputParam;
import org.shunya.punter.annotations.OutputParam;
import org.shunya.punter.executors.ParallelTaskRunner;
import org.shunya.punter.gui.TaskObserver;
import org.shunya.punter.jpa.TaskData;
import org.shunya.punter.utils.FieldProperties;
import org.shunya.punter.utils.FieldPropertiesMap;
import org.shunya.server.component.RestClient;
import org.shunya.server.component.StaticDaoFacade;

import javax.xml.bind.JAXBException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.*;

import static java.util.Arrays.asList;

public abstract class Tasks implements Serializable {
    protected Map<String, Object> sessionMap;
    private FieldPropertiesMap outputParams;
    private FieldPropertiesMap inputParams;
    private FieldPropertiesMap overrideInputParams;
    protected TaskData taskDao;
    protected StaticDaoFacade staticDaoFacade;
    private transient ConsoleHandler cHandler = null;
    private transient MemoryHandler mHandler = null;
    private transient Level loggingLevel = Level.FINE;
    protected StringBuilder strLogger;
    private boolean doVariableSubstitution = false;
    private LogListener logListener;
    private String hosts;
    protected RestClient restClient = new RestClient();
    protected TaskObserver observer;

    public void setDoVariableSubstitution(boolean doVariableSubstitution) {
        this.doVariableSubstitution = doVariableSubstitution;
    }

    public static final ThreadLocal<Logger> LOGGER = new ThreadLocal<Logger>() {
        @Override
        protected Logger initialValue() {
            Logger logger = Logger.getLogger("Logger for " + Thread.currentThread().getName());
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
                //                    String msg = new Date(record.getMillis()) + " [" + record.getLevel() + "] " + record.getMessage();
                final String msg = record.getMessage() + "\r";
                strLogger.append(msg);
                if (logListener != null) {
                    logListener.log(msg);
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        }, 2, loggingLevel);
        LOGGER.get().addHandler(mHandler);
//	    LOGGER.get().addHandler(cHandler);
        LOGGER.get().setUseParentHandlers(false);
    }

    public void setLoggingLevel(Level loggingLevel) {
        this.loggingLevel = loggingLevel;
    }

    public StaticDaoFacade getStaticDaoFacade() {
        return staticDaoFacade;
    }

    public void setStaticDaoFacade(StaticDaoFacade staticDaoFacade) {
        this.staticDaoFacade = staticDaoFacade;
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
        Map<String, FieldProperties> fieldPropertiesMap = new HashMap<>();
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
        Map<String, FieldProperties> fieldPropertiesMap = new HashMap<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(OutputParam.class)) {
                OutputParam ann = field.getAnnotation(OutputParam.class);
                fieldPropertiesMap.put(field.getName(), new FieldProperties(field.getName(), "", "", false));
            }
        }
        return new FieldPropertiesMap(fieldPropertiesMap);
    }

    public static Tasks getTask(TaskData taskData) {
        try {
            Class<?> clz = Class.forName(taskData.getClassName());
            Tasks task = (Tasks) clz.newInstance();
            task.setOutputParams(taskData.getOutputParamsAsObject());
            task.setInputParams(taskData.getInputParamsAsObject());
            return task;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void substituteParams() throws Exception {
        substituteParams(getInputParams());
        if (getOverrideInputParams() != null)
            substituteParams(getOverrideInputParams());
        if (taskDao.getHosts() != null && taskDao.getHosts().contains("#{")) {
            taskDao.setHosts(substituteVariables(taskDao.getHosts(), sessionMap));
        }
    }

    private void substituteParams(FieldPropertiesMap inputParams) throws Exception {
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(InputParam.class)) {
                try {
                    field.setAccessible(true);
                    if (inputParams.get(field.getName()) != null) {
                        String fieldValue = inputParams.get(field.getName()).getValue();
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
                            inputParams.get(field.getName()).setValue(fieldValue);
                        }
                    }
                } catch (IllegalArgumentException e) {
//					LOGGER.get().severe(e.toString());
                    throw e;
                } catch (IllegalAccessException e) {
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
        AtomicBoolean status = new AtomicBoolean(false);
        substituteParams();
        if (getHosts() == null || getHosts().trim().isEmpty()) {
            status.set(run());
            substituteResult();
        } else {
            taskDao.setInputParamsAsObject(getInputParams());
            List<Callable> tasks = new ArrayList<>();
            List<Boolean> statuses = new ArrayList<>();
            asList(taskDao.getHosts().split("[,;]")).forEach(host -> tasks.add(() -> restClient.executeRemoteTask(taskDao, host.trim())));
            new ParallelTaskRunner().execute(tasks, resultsMap -> {
                boolean jobStatus = (boolean) resultsMap.get("status");
                statuses.add(jobStatus);
                sessionMap.putAll(resultsMap);
                strLogger.append(resultsMap.get("logs"));
            }).shutdown();
            status.set(true);
            statuses.forEach(jobStatus -> status.set(status.get() & jobStatus));
        }
        return status.get();
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
        List<String> vars = new ArrayList<>();
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

    public Object getSessionObject(String key) {
        return sessionMap.get(key);
    }

    public void setSessionObject(String key, Object obj) {
        sessionMap.put(key, obj);
    }

    public void setOverrideInputParams(FieldPropertiesMap overrideInputParams) {
        this.overrideInputParams = overrideInputParams;
    }

    public FieldPropertiesMap getOverrideInputParams() {
        return overrideInputParams;
    }

    public void setLogListener(LogListener logListener) {
        this.logListener = logListener;
    }

    public void showLog() {
        if (logListener != null)
            logListener.showLog();
    }

    public void disposeLogs() {
        if (logListener != null)
            logListener.disposeLogs();
    }

    public String getHosts() {
        return hosts;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    public void setObserver(TaskObserver observer) {
        this.observer = observer;
    }

    public TaskObserver getObserver() {
        return observer;
    }
}