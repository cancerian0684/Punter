package com.shunya.punter.executors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.shunya.punter.gui.AppSettings;

public class ProcessExecutor {
    private static ProcessExecutor processExecutor;
    private ExecutorService executor;
    private LinkedBlockingQueue<Runnable> workQueue;

    public synchronized static ProcessExecutor getInstance() {
        if (processExecutor == null) {
            processExecutor = new ProcessExecutor();
        }
        return processExecutor;
    }

    public ProcessExecutor() {
        workQueue = new LinkedBlockingQueue<Runnable>(10);
        executor = new ThreadPoolExecutor(AppSettings.getInstance().getMaxExecutorSize(), AppSettings.getInstance().getMaxExecutorSize(),
                0L, TimeUnit.MILLISECONDS,
                workQueue);
    }

    public void submitProcess(final com.shunya.punter.tasks.Process process) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    process.execute();
                } catch (Throwable te) {
                    te.printStackTrace();
                }
            }
        });
    }

    public boolean isActive() {
        return ((ThreadPoolExecutor) executor).getActiveCount() > 0;
        //getQueueSize>0 && getActiveCount>0
    }

    public void shutdown() {
        try {
            executor.shutdown();
            executor.awaitTermination(2, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}