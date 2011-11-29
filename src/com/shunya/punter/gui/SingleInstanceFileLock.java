package com.sapient.punter.gui;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * Created by IntelliJ IDEA.
 * User: mchan2
 * Date: 10/3/11
 * Time: 10:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class SingleInstanceFileLock {
    private File file;
    private FileChannel channel;
    private FileLock lock;
    public final String RING_ON_REQUEST_LOCK;

    public SingleInstanceFileLock(final String lockFile) {
        RING_ON_REQUEST_LOCK = lockFile;
    }

    public boolean checkIfAlreadyRunning() {
        try {
            file = new File(RING_ON_REQUEST_LOCK);
            if (file.exists()) {
                file.delete();
            }
            channel = new RandomAccessFile(file, "rw").getChannel();
            lock = channel.tryLock();
            if (lock == null) {
                channel.close();
                throw new RuntimeException("Only 1 instance of Punter can run.");
            }
            file.deleteOnExit();
            // Add shutdown hook to release lock when application shutdown
            ShutdownHook shutdownHook = new ShutdownHook();
            Runtime.getRuntime().addShutdownHook(shutdownHook);
            System.out.println("Running");
        } catch (IOException e) {
//            throw new RuntimeException("Could not start process.", e);
            return true;
        }
        return false;
    }

    public void unlockFile() {
        try {
            if (lock != null) {
                lock.release();
                channel.close();
                file.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ShutdownHook extends Thread {
        public void run() {
            unlockFile();
        }
    }
}