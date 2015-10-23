package org.shunya.punter.tasks;

import org.shunya.punter.annotations.InputParam;
import org.shunya.punter.gui.AppSettings;
import org.shunya.punter.gui.LogWindow;
import org.shunya.punter.gui.ProcessObserver;
import org.shunya.punter.gui.TaskObserver;
import org.shunya.punter.jpa.ProcessHistory;
import org.shunya.punter.jpa.RunState;
import org.shunya.punter.jpa.RunStatus;
import org.shunya.punter.jpa.TaskHistory;
import org.shunya.punter.utils.DevEmailService;
import org.shunya.punter.utils.FieldProperties;
import org.shunya.punter.utils.FieldPropertiesMap;
import org.shunya.punter.utils.StringUtils;
import org.shunya.server.component.DBService;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Process implements Serializable {
    private DBService dbService;
    private Map sessionMap = new ConcurrentHashMap<>(50);
    private transient TaskObserver observer;
    private boolean failed = false;
    private FieldPropertiesMap inputParams;
    private FieldPropertiesMap overrideInputParams;
    private ProcessHistory processHistory;
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
    private Logger processLogger;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public Process() {
    }

    public void addObserver(ProcessObserver po) {
        this.po = po;
    }

    public void beforeProcessStart() {
        sessionMap.putAll(AppSettings.getInstance().getSessionMap());
        System.err.println("Emails to notify : " + emailsToNotify);
        processHistory.setRunState(RunState.RUNNING);
        po.update(processHistory);
    }

    public void afterProcessFinish() {
        dbService.saveProcessHistory(processHistory);
        po.update(processHistory);
        po.processCompleted();
        sessionMap.put("status", processHistory.getRunStatus());
        if (emailsToNotify != null && !emailsToNotify.isEmpty()) {
            if (emailsOnFailureOnly && processHistory.getRunStatus() == RunStatus.SUCCESS)
                return;
            try {
                if (processLogger != null) {
                    processLogger.log(Level.INFO, "Sending Task Report Mail to : " + emailsToNotify);
                }
                StringBuilder processLogs = new StringBuilder(10000);
                for (TaskHistory th : processHistory.getTaskHistoryList()) {
                    processLogs.append(th.getLogs() + "\r\n");
                }
                DevEmailService.getInstance().sendEmail("Punter Task : [" + processHistory.getRunStatus() + "] " + processHistory.getName(), emailsToNotify, processLogs.toString(), Collections.<File>emptyList());
                if (processLogger != null) {
                    processLogger.log(Level.INFO, "Mail Sent.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setLoggingLevel(Logger processLogger) {
        try {
            processLogger.setLevel(Level.parse(loggingLevel));
        } catch (Exception e) {
            processLogger.setLevel(Level.INFO);
        }
    }

    public Map execute() throws Exception {
        substituteParams();
        beforeProcessStart();
        executeProcessTasks();
        afterProcessFinish();
        return sessionMap;
    }

    private void executeProcessTasks() throws JAXBException {
        processHistory.setStartTime(new Date());
        processHistory.setRunStatus(RunStatus.RUNNING);
        AtomicBoolean keepRunning = new AtomicBoolean(true);
        AtomicInteger progressCounter = new AtomicInteger(0);
        final TreeMap<Integer, List<TaskHistory>> treeMap = new TreeMap(processHistory.getTaskHistoryList().stream().collect(Collectors.groupingBy(t -> t.getSequence())));
        treeMap.forEach((k, ol) -> {
            System.out.println("running sequence = " + k);
            if (ol.size() > 1) {
                System.out.println("Running Tasks in parallel " + ol);
                List<Future> futures = new ArrayList<>();
                ol.forEach(th -> {
                    futures.add(executorService.submit(() -> runTask(keepRunning, progressCounter, th)));
                });
                futures.forEach(f -> {
                    try {
                        f.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                System.out.println("Running Task Sequentially " + ol);
                runTask(keepRunning, progressCounter, ol.get(0));
            }
        });
        executorService.shutdownNow();
        processHistory.setRunState(RunState.COMPLETED);
        if (!failed) {
            processHistory.setRunStatus(RunStatus.SUCCESS);
            if (!alwaysRaiseAlert)
                processHistory.setClearAlert(true);
        } else {
            processHistory.setRunStatus(RunStatus.FAILURE);
        }
        processHistory.setFinishTime(new Date());
    }

    private void runTask(AtomicBoolean keepRunning, AtomicInteger progressCounter, TaskHistory th) {
        Tasks task = Tasks.getTask(th.getTask());
        task.setObserver(observer);
        task.setTaskHistory(th);
        task.setHosts(th.getTask().getHosts());
        task.setDbService(dbService);
        task.setTaskDao(th.getTask());
        task.setSessionMap(sessionMap);
        task.setOverrideInputParams(overrideInputParams);
        task.setDoVariableSubstitution(doVariableSubstitution);
        task.setLogListener(new LogWindow(5000, th.getId() + " - " + th.getTask().getDescription() + " Logs"));
        th.setRunState(RunState.RUNNING);
        th.setRunStatus(RunStatus.RUNNING);
        th.setStartTime(new Date());
        th.setTasks(task);
        boolean status = false;
        if ((keepRunning.get() && !task.getTaskDao().isFailOver()) || (task.getTaskDao().isFailOver() && failed)) {
            try {
                task.beforeTaskStart();
                processLogger = task.LOGGER.get();
                setLoggingLevel(processLogger);
                processLogger.log(Level.INFO, "started executing task.." + task.getTaskDao().getSequence() + " - " + task.getTaskDao().getName());
                status = task.execute();
                processLogger.log(Level.INFO, "Finished executing task.." + task.getTaskDao().getSequence() + " - " + task.getTaskDao().getName());
                th.setFinishTime(new Date());
                if (stopOnTaskFailure & !status) {
                    keepRunning.set(false);
                }
                if (!status) {
                    failed = true;
                }
            } catch (Throwable e) {
                failed = true;
                if (stopOnTaskFailure) {
                    keepRunning.set(false);
                }
                task.LOGGER.get().log(Level.SEVERE, StringUtils.getExceptionStackTrace(e));
                task.LOGGER.get().log(Level.FINE, "Finished executing task.." + task.getTaskDao().getSequence() + " - " + task.getTaskDao().getName());
            } finally {
                task.afterTaskFinish();
            }
            th.setStatus(status);
            if (status)
                th.setRunStatus(RunStatus.SUCCESS);
            else
                th.setRunStatus(RunStatus.FAILURE);
            th.setRunState(RunState.COMPLETED);
            th.setProgress(100);
            th.setLogs(task.getMemoryLogs());
            observer.saveTaskHistory(th);
        } else {
            th.setRunState(RunState.NOT_RUN);
            th.setRunStatus(RunStatus.NOT_RUN);
            th.setLogs("");
            observer.saveTaskHistory(th);
        }
        progressCounter.incrementAndGet();
        processHistory.setProgress(100 * progressCounter.get() / processHistory.getTaskHistoryList().size());
        po.update(processHistory);
        th.setTasks(null);
        task.setLogListener(null);
    }

    public void setTaskObservable(TaskObserver ts) {
        this.observer = ts;
    }

    public static FieldPropertiesMap listInputParams() {
        Field[] fields = Process.class.getDeclaredFields();
        System.out.println("Listing input params for process");
        Map<String, FieldProperties> inProp = new HashMap<String, FieldProperties>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(InputParam.class)) {
                InputParam ann = field.getAnnotation(InputParam.class);
                inProp.put(field.getName(), new FieldProperties(field.getName(), "", ann.description(), ann.required()));
            }
        }
        return new FieldPropertiesMap(inProp);
    }

    private void substituteParams() {
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(InputParam.class) && (inputParams.get(field.getName()) != null)) {
                try {
                    field.setAccessible(true);
                    String fieldValue = inputParams.get(field.getName()).getValue();
                    if (fieldValue.length() >= 1) {
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
                } catch (IllegalArgumentException e) {
                    Tasks.LOGGER.get().severe(e.toString());
                } catch (IllegalAccessException e) {
                    Tasks.LOGGER.get().severe(e.toString());
                } catch (ParseException e) {
                    Tasks.LOGGER.get().severe(e.toString());
                } catch (Exception e) {
                    Tasks.LOGGER.get().severe(e.toString());
                }
            }
        }
    }

    public static Process getProcess(DBService DBService, FieldPropertiesMap props, ProcessHistory history, FieldPropertiesMap overrideInputParams) {
        Process process = new Process();
        process.dbService = DBService;
        process.inputParams = props;
        process.processHistory = history;
        process.overrideInputParams = overrideInputParams;
        return process;
    }

    public Map getSessionMap() {
        return sessionMap;
    }

    public static void main(String[] args) {
        listInputParams();
    }
}
