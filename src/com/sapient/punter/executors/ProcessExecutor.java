package com.sapient.punter.executors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
		executor = Executors.newFixedThreadPool(2);
	}
	public void submitProcess(final com.sapient.punter.tasks.Process proc){
		executor.submit(new Runnable() {
			@Override
			public void run() {
				proc.execute();
			}
		});
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
