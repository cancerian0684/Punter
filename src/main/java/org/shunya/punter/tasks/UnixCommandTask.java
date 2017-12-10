package org.shunya.punter.tasks;

import ch.qos.logback.classic.Logger;
import org.shunya.punter.annotations.InputParam;
import org.shunya.punter.annotations.PunterTask;
import org.shunya.punter.utils.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.lang.Process;
import java.util.concurrent.atomic.AtomicBoolean;

@PunterTask(author = "munishc", name = "UnixCommandTask", documentation = "src/main/resources/docs/SystemCommandTask.html")
public class UnixCommandTask extends Tasks {
    @InputParam(required = true, description = "/Users/MunishChandel/Documents/munish/shunya-healthcare")
    public String workingDir;
    @InputParam(required = true, description = "./gradlew build -x test")
    public String command;
    @InputParam(required = false, description = "Build Success")
    public String successMessage;
    @InputParam(required = false, description = "Should wait for the Process termination or not ?")
    public boolean waitForTerminate = true;

    @Override
    public boolean run() {
        final AtomicBoolean status = new AtomicBoolean(true);
        try {
            String[] commands = command.split("[ ]");
            ProcessBuilder builder = new ProcessBuilder(commands);
            builder.directory(new File(workingDir));
            builder.redirectErrorStream(true);
            Process child = builder.start();

            final Logger logger = LOGGER.get();
            Thread captureProcessStreams = new Thread(() -> {
                try {
                    startOutputAndErrorReadThreads(child.getInputStream(), child.getErrorStream(), logger);
                } catch (AssertionMessageException e) {
                    status.set(false);
                    logger.error(e.getMessage());
                } catch (Exception e) {
                    status.set(false);
                    logger.error(StringUtils.getExceptionStackTrace(e));
                }
            });
            captureProcessStreams.start();
            captureProcessStreams.join();

            if (waitForTerminate) {
                logger.info("Waiting for the process to terminate");
                child.waitFor();
            }

            getTaskHistory().setActivity("finished all tasks");
            getObserver().update(getTaskHistory());
        } catch (Exception e) {
            status.set(false);
            LOGGER.get().error(StringUtils.getExceptionStackTrace(e));
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