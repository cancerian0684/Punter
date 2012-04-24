package com.shunya.punter.tasks;

import com.shunya.punter.annotations.InputParam;
import com.shunya.punter.annotations.PunterTask;

import java.io.InputStream;
import java.util.logging.Level;

@PunterTask(author = "munishc", name = "SystemCommandTask", documentation = "docs/SystemCommandTask.html")
public class SystemCommandTask extends Tasks {
    @InputParam(required = true, description = "imp 'DAISY4/Welcome1@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(PORT=1523)(HOST=xldn2738dor.ldn.swissbank.com)))(CONNECT_DATA=(SERVICE_NAME=DELSHRD1)))' fromuser=AIS1 touser=DAISY2 ignore=y log=C:\\import_daisy.log file=#{dumpFile}")
    public String systemCommand;
    @InputParam(required = false, description = "Import terminated successfully")
    public String successMessage;
    @InputParam(required = false, description = "Should wait for the Process termination or not ?")
    public boolean waitForTerminate = true;

    @Override
    public boolean run() {
        boolean status;
        try {
            java.lang.Process process = Runtime.getRuntime().exec(systemCommand.split("[\n]"));
            status = startOutputAndErrorReadThreads(process.getInputStream(), process.getErrorStream());
            process.getOutputStream().close();
            if (waitForTerminate) {
                LOGGER.get().log(Level.INFO, "Waiting for the process to terminate");
                process.waitFor();
            }
        } catch (Exception e) {
            status = false;
            LOGGER.get().log(Level.SEVERE, e.getMessage());
        }
        return status;
    }

    private boolean startOutputAndErrorReadThreads(InputStream processOutputStream, InputStream processErrorStream) throws Exception {
        StringBuffer commandOutputBuffer = new StringBuffer();
        AsynchronousStreamReader asynchronousCommandOutputReaderThread = new AsynchronousStreamReader(processOutputStream, commandOutputBuffer, new MyLogDevice(LOGGER.get()), "OUTPUT");
        asynchronousCommandOutputReaderThread.start();
        StringBuffer commandErrorBuffer = new StringBuffer();
        AsynchronousStreamReader asynchronousCommandErrorReaderThread = new AsynchronousStreamReader(processErrorStream, commandErrorBuffer, new MyLogDevice(LOGGER.get()), "ERROR");
        asynchronousCommandErrorReaderThread.start();
        asynchronousCommandOutputReaderThread.join();
        asynchronousCommandErrorReaderThread.join();
        return successMessage == null || successMessage.isEmpty() || commandErrorBuffer.toString().contains(successMessage) || commandOutputBuffer.toString().contains(successMessage);
    }
}