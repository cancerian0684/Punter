package org.shunya.punter.tasks;

import ch.qos.logback.classic.Logger;
import org.shunya.punter.annotations.InputParam;
import org.shunya.punter.annotations.PunterTask;
import org.shunya.punter.utils.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.lang.Process;
import java.util.Scanner;
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

    StringBuffer commandOutputBuffer = new StringBuffer();
    StringBuffer commandErrorBuffer = new StringBuffer();

    @Override
    public boolean run() {
        final AtomicBoolean status = new AtomicBoolean(true);
        getTaskHistory().setActivity("Connecting to Shell");
        getObserver().update(getTaskHistory());
        try(Scanner stk = new Scanner(command).useDelimiter("\r\n|\n\r|\r|\n")) {
            while (stk.hasNext()) {
                String singleCommand = stk.next().trim();
                getTaskHistory().setActivity(singleCommand+" ...");
                getObserver().update(getTaskHistory());
                String[] commands = singleCommand.split("[ ]");
                ProcessBuilder builder = new ProcessBuilder(commands);
                builder.directory(new File(workingDir));
                builder.redirectErrorStream(true);
                Process child = builder.start();
                final Logger logger = LOGGER.get();
                Thread captureProcessStreams = new Thread(() -> {
                    try {
                        startOutputAndErrorReadThreads(child.getInputStream(), child.getErrorStream(), logger);
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
            }
            if (!assertMessage(commandOutputBuffer, commandErrorBuffer)) {
                status.set(false);
                LOGGER.get().error("output does not contain required message string - ["+successMessage+"]");
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
        AsynchronousStreamReader asynchronousCommandOutputReaderThread = new AsynchronousStreamReader(processOutputStream, commandOutputBuffer, new MyLogDevice(logger), "OUTPUT");
        asynchronousCommandOutputReaderThread.start();
        AsynchronousStreamReader asynchronousCommandErrorReaderThread = new AsynchronousStreamReader(processErrorStream, commandErrorBuffer, new MyLogDevice(logger), "ERROR");
        asynchronousCommandErrorReaderThread.start();
        asynchronousCommandOutputReaderThread.join();
        asynchronousCommandErrorReaderThread.join();
    }

    private boolean assertMessage(StringBuffer commandOutputBuffer, StringBuffer commandErrorBuffer) {
        return successMessage == null || successMessage.isEmpty() || commandErrorBuffer.toString().contains(successMessage) || commandOutputBuffer.toString().contains(successMessage);
    }
}