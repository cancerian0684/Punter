package com.sapient.kb.gui;

import java.util.concurrent.TimeUnit;

import com.sapient.punter.gui.AppSettings;

public class PunterDelayQueue<T> {
    private T element;
    private volatile long expiryTime = System.currentTimeMillis();

    public synchronized void put(T element, int delay) {
        this.element = element;
        this.expiryTime = System.currentTimeMillis() + delay;
        notifyAll();
    }

    public synchronized T take() {
        T tmp = null;
        int counter = AppSettings.getInstance().getKeyStrokeFlush();
        while (element == null || tmp == null || remainingTime() > 0) {
            try {
                if (element == null)
                    wait();
                else
                    wait(remainingTime());
                tmp = element;
                counter--;
                if (counter < 0 && tmp != null) {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        element = null;
        notifyAll();
        return tmp;
    }

    private long remainingTime() {
        long remaining = expiryTime - System.currentTimeMillis();
        return remaining > 0 ? remaining : 0;
    }

    public static void main(String[] args) {
        final PunterDelayQueue<String> pdq = new PunterDelayQueue<String>();
        Thread producer = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    pdq.put("munish", 100);
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        producer.start();

        Thread consumer = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    System.out.println(pdq.take());
                }
            }
        });
        consumer.start();
    }
}
