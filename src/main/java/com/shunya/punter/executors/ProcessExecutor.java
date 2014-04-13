package com.shunya.punter.executors;

import com.shunya.punter.gui.AppSettings;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
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
        workQueue = new LinkedBlockingQueue<>(100);
        executor = new ThreadPoolExecutor(AppSettings.getInstance().getMaxExecutorSize(), AppSettings.getInstance().getMaxExecutorSize(),
                0L, TimeUnit.MILLISECONDS, workQueue);
    }

    public Future<Map> submitProcess(final com.shunya.punter.tasks.Process process) {
        return executor.submit(() -> {
            try {
                jobCount.incrementAndGet();
                return process.execute();
            } catch (Throwable te) {
                te.printStackTrace();
                return new HashMap();
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