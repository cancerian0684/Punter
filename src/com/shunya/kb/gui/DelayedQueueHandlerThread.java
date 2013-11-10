package com.shunya.kb.gui;

import com.shunya.punter.gui.AppSettings;

public class DelayedQueueHandlerThread<T> extends Thread {
    public interface CallBackHandler<T> {
        void process(T element);
    }

    private DelaySkipQueue<T> delaySkipQueue;
    private CallBackHandler callBackHandler;

    DelayedQueueHandlerThread(CallBackHandler callBackHandler) {
        this.callBackHandler = callBackHandler;
        this.delaySkipQueue = new DelaySkipQueue<>(AppSettings.getInstance().getKeyStrokeFlush());
        setPriority(Thread.MIN_PRIORITY);
        setDaemon(true);
    }

    public void put(T element) {
        delaySkipQueue.put(element);
    }

    @Override
    public void run() {
        System.err.println("Delayed Queue Thread started.");
        while (true) {
            T element;
            try {
                element = delaySkipQueue.take();
                try {
                    callBackHandler.process(element);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
