package com.sapient.kb.gui;

import java.util.concurrent.TimeUnit;

import com.sapient.punter.gui.AppSettings;

public class PunterDelayQueue {
private String element;
private volatile long expiryTime=System.currentTimeMillis();
public synchronized void put(String element,int delay){
	this.element=element;
	this.expiryTime=System.currentTimeMillis()+delay;
	notifyAll();
}
public synchronized String take(){
	String tmp=null;
	int counter=AppSettings.getInstance().getKeyStrokeFlush();
	while(element==null||tmp==null||remainingTime()>0){
		try{
			if(element==null)
				wait();
			else
				wait(remainingTime());
			tmp=element;
			counter--;
			if(counter<0&&tmp!=null){
				break;
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	element=null;
	notifyAll();
	return tmp;
}
private long remainingTime(){
	long remaining=expiryTime-System.currentTimeMillis();
	return remaining;
}
public static void main(String[] args) {
	final PunterDelayQueue pdq=new PunterDelayQueue();
	Thread p=new Thread(new Runnable() {
		
		@Override
		public void run() {
		while(true){
			pdq.put("munish", 100);
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		}
	});
	p.start();
	
	Thread c=new Thread(new Runnable() {
		@Override
		public void run() {
		while(true){
			System.out.println(pdq.take());
		}
		}
	});
	c.start();
}
}
