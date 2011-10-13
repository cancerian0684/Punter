package com.sapient.kb.gui;

import com.sapient.punter.gui.AppSettings;

public class PunterDelayedQueueHandlerThread <T> extends Thread {
    public interface PunterDelayedQueueListener <T> {
        void process(T element);
    }

    private PunterDelayQueue<T> punterDelayQueue;
    private PunterDelayedQueueListener punterDelayedQueueListener;

    PunterDelayedQueueHandlerThread(PunterDelayedQueueListener punterDelayedQueueListener) {
        this.punterDelayedQueueListener = punterDelayedQueueListener;
        this.punterDelayQueue = new PunterDelayQueue<T>(AppSettings.getInstance().getKeyStrokeFlush());
        setPriority(Thread.MIN_PRIORITY);
		setDaemon(true);
		start();
		System.err.println("Punter Delayed Queue Thread started.");
    }
    public void put(T element){
            punterDelayQueue.put(element, AppSettings.getInstance().getMaxKeyStrokeDelay());
    }
    @Override
    public void run() {
        while (true) {
            T element = punterDelayQueue.take();
            punterDelayedQueueListener.process(element);
        }
    }
}
