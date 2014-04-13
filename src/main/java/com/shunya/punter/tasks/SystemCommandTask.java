package com.shunya.punter.tasks;

import com.shunya.punter.annotations.InputParam;
import com.shunya.punter.annotations.PunterTask;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

@PunterTask(author = "munishc", name = "SystemCommandTask", documentation = "src/main/resources/docs/SystemCommandTask.html")
public class SystemCommandTask extends Tasks {
    @InputParam(required = true, description = "imp 'DAISY4/Welcome1@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(PORT=1523)(HOST=xldn2738dor.ldn.swissbank.com)))(CONNECT_DATA=(SERVICE_NAME=DELSHRD1)))' fromuser=AIS1 touser=DAISY2 ignore=y log=C:\\import_daisy.log file=#{dumpFile}")
    public String systemCommand;
    @InputParam(required = false, description = "Import terminated successfully")
    public String successMessage;
    @InputParam(required = false, description = "Should wait for the Process termination or not ?")
    public boolean waitForTerminate = true;

    @Override
    public boolean run() {
        final boolean[] status = new boolean[1];
        try {
            String[] commands = systemCommand.split("[\n]");
            final java.lang.Process child = Runtime.getRuntime().exec("cmd /k");

            final Logger logger = LOGGER.get();
            Thread captureProcessStreams = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        status[0] = startOutputAndErrorReadThreads(child.getInputStream(), child.getErrorStream(), logger);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            captureProcessStreams.start();

            OutputStream out = child.getOutputStream();

            for (String command : commands) {
                out.write((command + "\r\n").getBytes());
                out.flush();
            }

//            out.write("exit\r\n".getBytes());
//            out.flush();
            out.close();

            captureProcessStreams.join();

            if (waitForTerminate) {
                LOGGER.get().log(Level.INFO, "Waiting for the process to terminate");
                child.waitFor();
            }
        } catch (Exception e) {
            status[0] = false;
            LOGGER.get().log(Level.SEVERE, e.getMessage());
        }
        return status[0];
    }

    private boolean startOutputAndErrorReadThreads(InputStream processOutputStream, InputStream processErrorStream, Logger logger) throws Exception {
        StringBuffer commandOutputBuffer = new StringBuffer();
        AsynchronousStreamReader asynchronousCommandOutputReaderThread = new AsynchronousStreamReader(processOutputStream, commandOutputBuffer, new MyLogDevice(logger), "OUTPUT");
        asynchronousCommandOutputReaderThread.start();
        StringBuffer commandErrorBuffer = new StringBuffer();
        AsynchronousStreamReader asynchronousCommandErrorReaderThread = new AsynchronousStreamReader(processErrorStream, commandErrorBuffer, new MyLogDevice(logger), "ERROR");
        asynchronousCommandErrorReaderThread.start();
        asynchronousCommandOutputReaderThread.join();
        asynchronousCommandErrorReaderThread.join();
        return successMessage == null || successMessage.isEmpty() || commandErrorBuffer.toString().contains(successMessage) || commandOutputBuffer.toString().contains(successMessage);
    }
}