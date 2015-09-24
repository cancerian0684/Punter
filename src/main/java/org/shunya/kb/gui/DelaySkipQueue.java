package org.shunya.kb.gui;

import java.util.concurrent.TimeUnit;

public class DelaySkipQueue<T> {
    private final int maxDelay;
    private final int maxKeyStrokes;
    private final Latch latch;
    private volatile T element;
    private volatile long expiryTime;
    private volatile int pendingKeyStroke;


    public DelaySkipQueue(int maxDelay, int maxKeyStrokes) {
        this.maxDelay = maxDelay;
        this.maxKeyStrokes = maxKeyStrokes;
        this.latch = new Latch();
        this.expiryTime = System.currentTimeMillis() + maxDelay;
    }

    public void put(T element) {
        this.element = element;
        ++pendingKeyStroke;
        if (element != null)
            latch.release();
    }

    public T take() throws InterruptedException {
        latch.await();
        long currentTime = System.currentTimeMillis();
        while (currentTime < expiryTime && pendingKeyStroke < maxKeyStrokes) {
            latch.lock();
            try {
//                System.out.println("acquire lock");
                latch.await(expiryTime - currentTime);
//                System.out.println("release lock");
            } catch (Exception e) {
                System.err.println("exception happened." + e.getMessage());
            }
            currentTime = System.currentTimeMillis();
//            Thread.sleep(expiryTime - currentTime);
        }
        expiryTime = System.currentTimeMillis() + maxDelay;
        pendingKeyStroke = 0;
        latch.lock();
        return element;
    }

    public static void main(String[] args) {
        final DelaySkipQueue<String> pdq = new DelaySkipQueue<>(400, 4);
        Thread producer = new Thread(() -> {
            int i = 0;
            while (i < 5) {
                pdq.put("munish" + i++);
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        producer.start();

        Thread consumer = new Thread(() -> {
            while (true) {
                try {
                    System.out.println(pdq.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        consumer.start();
    }

    static class Latch {
        boolean state = false;

        public synchronized void release() {
            state = true;
            notifyAll();
        }

        public synchronized void await() throws InterruptedException {
            while (!state)
                wait();
        }

        public synchronized void await(long millis) throws InterruptedException {
            if (!state)
                wait(millis);
        }

        public synchronized void lock() {
            state = false;
        }
    }
}
