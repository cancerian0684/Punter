package com.shunya.punter.tasks;

import com.shunya.kb.jpa.StaticDaoFacadeRemote;
import com.shunya.punter.annotations.InputParam;
import com.shunya.punter.gui.AppSettings;
import com.shunya.punter.gui.ProcessObserver;
import com.shunya.punter.gui.TaskObserver;
import com.shunya.punter.jpa.ProcessHistory;
import com.shunya.punter.jpa.RunState;
import com.shunya.punter.jpa.RunStatus;
import com.shunya.punter.jpa.TaskHistory;
import com.shunya.punter.utils.EmailService;
import com.shunya.punter.utils.FieldProperties;
import com.shunya.punter.utils.FieldPropertiesMap;
import com.shunya.punter.utils.StringUtils;

import javax.swing.text.*;
import javax.xml.bind.JAXBException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Process implements Serializable {
    private List<Tasks> taskList = new ArrayList<Tasks>();
    private Map sessionMap = new HashMap<String, Object>();
    private transient TaskObserver ts;
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
            }

            ;
        };
        // System.err.println("Emails to notify : "+emailsToNotify);
        processHistory.setRunState(RunState.RUNNING);
        po.update(processHistory);
    }

    public void afterProcessFinish() {
        StaticDaoFacadeRemote.getInstance().saveProcessHistory(processHistory);
        po.update(processHistory);
        po.processCompleted();
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
                EmailService.getInstance().sendEMail("Punter Task : [" + processHistory.getRunStatus() + "] " + processHistory.getName(), emailsToNotify, processLogs.toString());
                if (processLogger != null) {
                    processLogger.log(Level.INFO, "Mail Sent.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setLoggingLevel(Logger processLogger) {
        try{processLogger.setLevel(Level.parse(loggingLevel));}catch (Exception e){
            processLogger.setLevel(Level.INFO);
        }
    }

    public void execute() throws Exception {
        substituteParams();
        beforeProcessStart();
        executeProcessTasks();
        afterProcessFinish();
    }

    private void executeProcessTasks() throws JAXBException {
        processHistory.setStartTime(new Date());
        processHistory.setRunStatus(RunStatus.RUNNING);
        processHistory.setLogDocument(logDocument);
        boolean keepRunning = true;
        int progressCounter = 0;
        for (TaskHistory th : processHistory.getTaskHistoryList()) {
            Tasks task = Tasks.getTask(th.getTask());
            task.setTaskDao(th.getTask());
            task.setSessionMap(sessionMap);
            task.setOverrideInputParams(overrideInputParams);
            task.setLogDocument(logDocument);
            task.setDoVariableSubstitution(doVariableSubstitution);
            th.setRunState(RunState.RUNNING);
            th.setRunStatus(RunStatus.RUNNING);
            th.setStartTime(new Date());
            boolean status = false;
            if ((keepRunning && !task.getTaskDao().isFailOver()) || (task.getTaskDao().isFailOver() && failed)) {
                try {
                    task.beforeTaskStart();
                    processLogger = task.LOGGER.get();
                    setLoggingLevel(processLogger);
                    processLogger.log(Level.FINE, "started executing task.." + task.getTaskDao().getSequence() + " - " + task.getTaskDao().getName());
                    status = task.execute();
                    processLogger.log(Level.FINE, "Finished executing task.." + task.getTaskDao().getSequence() + " - " + task.getTaskDao().getName());
                    th.setFinishTime(new Date());
                    if (stopOnTaskFailure) {
                        keepRunning = status;
                    }
                    if (!status) {
                        failed = true;
                    }
                } catch (Throwable e) {
                    failed = true;
                    if (stopOnTaskFailure) {
                        keepRunning = false;
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
                th.setLogs(task.getMemoryLogs());
                ts.saveTaskHistory(th);
            } else {
                th.setRunState(RunState.NOT_RUN);
                th.setRunStatus(RunStatus.NOT_RUN);
                th.setLogs("");
                ts.saveTaskHistory(th);
            }
            progressCounter++;
            processHistory.setProgress(100 * progressCounter / processHistory.getTaskHistoryList().size());
            po.update(processHistory);

        }
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

    public void setTaskObservable(TaskObserver ts) {
        this.ts = ts;
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

    public static Process getProcess(FieldPropertiesMap props, ProcessHistory history, FieldPropertiesMap overrideInputParams) {
        Process process = new Process();
        process.inputParams = props;
        process.processHistory = history;
        process.overrideInputParams=overrideInputParams;
        return process;
    }

    public static void main(String[] args) {
        listInputParams();
    }
}
