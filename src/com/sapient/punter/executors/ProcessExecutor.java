package com.sapient.punter.executors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sapient.punter.gui.AppSettings;

public class ProcessExecutor {
	private static ProcessExecutor processExecutor;
	private ExecutorService executor;
	public synchronized static ProcessExecutor getInstance(){
		if(processExecutor==null){
			processExecutor=new ProcessExecutor();
		}
		return processExecutor;
	}
	public ProcessExecutor() {
		executor = new ThreadPoolExecutor(AppSettings.getInstance().getMaxExecutorSize(), AppSettings.getInstance().getMaxExecutorSize(),
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
	}
	public void submitProcess(final com.sapient.punter.tasks.Process proc){
		executor.submit(new Runnable() {
			@Override
			public void run() {
				proc.execute();
			}
		});
	}
	public boolean isActive(){
		return ((ThreadPoolExecutor)executor).getActiveCount()>0;
		//getQueueSize>0 && getActiveCount>0
	}
	public void shutdown(){
		try {
			executor.shutdown();
			executor.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
