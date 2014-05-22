package org.shunya.punter.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Process;
import java.util.logging.Logger;

/**
 * Created by munichan on 2/23/14.
 */
public class TestTask {

    private static String successMessage;

    public static final ThreadLocal<Logger> LOGGER = new ThreadLocal<Logger>() {
        @Override
        protected Logger initialValue() {
            Logger logger = Logger.getLogger("Logger for " + Thread.currentThread().getName());
            return logger;
        }
    };

    public static void main(String[] args) throws Exception {
        try {
//          Execute command
//          String command = "cmd /c start cmd.exe";
            String command = "cmd /k java -version";
            final Process child = Runtime.getRuntime().exec(command);

            Thread captureProcessStreams = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        startOutputAndErrorReadThreads(child.getInputStream(), child.getErrorStream());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            captureProcessStreams.start();
            // Get output stream to write from it
            OutputStream out = child.getOutputStream();

            out.write("dir \r\n".getBytes());
            out.flush();

            out.write("cd C:/ \r\n".getBytes());
            out.flush();

            out.write("dir \r\n".getBytes());
            out.flush();

            out.write("cd C:\\TDM\\TDM_trunk\\modules\\volume-data-generator \r\n".getBytes());
            out.flush();

            out.write("mvn clean install -DskipTests \r\n".getBytes());
            out.flush();

            out.write("exit \r\n".getBytes());
            out.flush();

            out.close();

            child.waitFor();
/*
            String response = IOUtils.toString(child.getInputStream());
            System.out.println("response = " + response);

            response = IOUtils.toString(child.getErrorStream());
            System.out.println("response = " + response);
*/

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean startOutputAndErrorReadThreads(InputStream processOutputStream, InputStream processErrorStream) throws Exception {
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
