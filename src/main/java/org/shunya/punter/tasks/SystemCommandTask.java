package org.shunya.punter.tasks;

import org.shunya.punter.annotations.InputParam;
import org.shunya.punter.annotations.PunterTask;
import org.shunya.punter.utils.StringUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

@PunterTask(author = "munishc", name = "SystemCommandTask", documentation = "src/main/resources/docs/SystemCommandTask.html")
public class SystemCommandTask extends Tasks {
    @InputParam(required = true, description = "imp 'DAISY4/Welcome1@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(PORT=1523)(HOST=hostname)))(CONNECT_DATA=(SERVICE_NAME=DELSHRD1)))' fromuser=AIS1 touser=DAISY2 ignore=y log=C:\\import_daisy.log file=#{dumpFile}")
    public String systemCommand;
    @InputParam(required = false, description = "Import terminated successfully")
    public String successMessage;
    @InputParam(required = false, description = "Should wait for the Process termination or not ?")
    public boolean waitForTerminate = true;

    @Override
    public boolean run() {
        final AtomicBoolean status = new AtomicBoolean(true);
        try {
            String[] commands = systemCommand.split("[\n]");
            final java.lang.Process child = Runtime.getRuntime().exec("cmd /k");

            final Logger logger = LOGGER.get();
            Thread captureProcessStreams = new Thread(() -> {
                try {
                    startOutputAndErrorReadThreads(child.getInputStream(), child.getErrorStream(), logger);
                } catch (AssertionMessageException e) {
                    status.set(false);
                    logger.log(Level.SEVERE, e.getMessage());
                } catch (Exception e) {
                    status.set(false);
                    logger.log(Level.SEVERE, StringUtils.getExceptionStackTrace(e));
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
                logger.log(Level.INFO, "Waiting for the process to terminate");
                child.waitFor();
            }
        } catch (Exception e) {
            status.set(false);
            LOGGER.get().log(Level.SEVERE, StringUtils.getExceptionStackTrace(e));
        }
        return status.get();
    }

    private void startOutputAndErrorReadThreads(InputStream processOutputStream, InputStream processErrorStream, Logger logger) throws AssertionMessageException, Exception {
        StringBuffer commandOutputBuffer = new StringBuffer();
        AsynchronousStreamReader asynchronousCommandOutputReaderThread = new AsynchronousStreamReader(processOutputStream, commandOutputBuffer, new MyLogDevice(logger), "OUTPUT");
        asynchronousCommandOutputReaderThread.start();
        StringBuffer commandErrorBuffer = new StringBuffer();
        AsynchronousStreamReader asynchronousCommandErrorReaderThread = new AsynchronousStreamReader(processErrorStream, commandErrorBuffer, new MyLogDevice(logger), "ERROR");
        asynchronousCommandErrorReaderThread.start();
        asynchronousCommandOutputReaderThread.join();
        asynchronousCommandErrorReaderThread.join();
        if (!assertMessage(commandOutputBuffer, commandErrorBuffer))
            throw new AssertionMessageException("output does not contain required message string - ["+successMessage+"]");
    }

    private boolean assertMessage(StringBuffer commandOutputBuffer, StringBuffer commandErrorBuffer) {
        return successMessage == null || successMessage.isEmpty() || commandErrorBuffer.toString().contains(successMessage) || commandOutputBuffer.toString().contains(successMessage);
    }
}