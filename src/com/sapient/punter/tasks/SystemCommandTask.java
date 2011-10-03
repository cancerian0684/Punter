package com.sapient.punter.tasks;

import com.sapient.punter.annotations.InputParam;
import com.sapient.punter.annotations.PunterTask;

import java.io.InputStream;
import java.util.logging.Level;

@PunterTask(author = "munishc", name = "SystemCommandTask", documentation = "com/sapient/punter/tasks/docs/SystemCommandTask.html")
public class SystemCommandTask extends Tasks {
    @InputParam(required = true, description = "imp 'DAISY4/Welcome1@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(PORT=1523)(HOST=xldn2738dor.ldn.swissbank.com)))(CONNECT_DATA=(SERVICE_NAME=DELSHRD1)))' fromuser=AIS1 touser=DAISY2 ignore=y log=C:\\import_daisy.log file=#{dumpFile}")
    private String systemCommand;
    @InputParam(required = false, description = "Import terminated successfully")
    private String shouldContainMsg;

    @Override
    public boolean run() {
        boolean status;
        try {
            java.lang.Process p = Runtime.getRuntime().exec(systemCommand);
            status = startOutputAndErrorReadThreads(p.getInputStream(), p.getErrorStream());
            p.waitFor();
        } catch (Exception e) {
            status = false;
            LOGGER.get().log(Level.SEVERE, e.getMessage());
        }
        return status;
    }

    private boolean startOutputAndErrorReadThreads(InputStream processOut, InputStream processErr) throws Exception {
        StringBuffer fCmdOutput = new StringBuffer();
        AsyncStreamReader fCmdOutputThread = new AsyncStreamReader(processOut, fCmdOutput, new myLogger(LOGGER.get()), "OUTPUT");
        fCmdOutputThread.start();
        StringBuffer fCmdError = new StringBuffer();
        AsyncStreamReader fCmdErrorThread = new AsyncStreamReader(processErr, fCmdError, new myLogger(LOGGER.get()), "ERROR");
        fCmdErrorThread.start();
        fCmdOutputThread.join();
        fCmdErrorThread.join();
        return fCmdError.toString().contains(shouldContainMsg) || fCmdOutput.toString().contains(shouldContainMsg);
    }
}