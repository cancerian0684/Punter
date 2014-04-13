package com.shunya.punter.executors;

import com.shunya.punter.gui.AppSettings;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ProcessExecutor {
    private static ProcessExecutor processExecutor;
    private ExecutorService executor;
    private LinkedBlockingQueue<Runnable> workQueue;
    private AtomicInteger jobCount = new AtomicInteger(0);

    public synchronized static ProcessExecutor getInstance() {
        if (processExecutor == null) {
            processExecutor = new ProcessExecutor();
        }
        return processExecutor;
    }

    public ProcessExecutor() {
        workQueue = new LinkedBlockingQueue<>(10);
        executor = new ThreadPoolExecutor(AppSettings.getInstance().getMaxExecutorSize(), AppSettings.getInstance().getMaxExecutorSize(),
                0L, TimeUnit.MILLISECONDS,
                workQueue);
    }

    public void submitProcess(final com.shunya.punter.tasks.Process process) {
        executor.submit(() -> {
            try {
                jobCount.incrementAndGet();
                process.execute();
            } catch (Throwable te) {
                te.printStackTrace();
            } finally {
                jobCount.decrementAndGet();
            }
        });
    }

    public boolean isActive() {
        return jobCount.get() > 0;
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