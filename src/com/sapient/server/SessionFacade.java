package com.sapient.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class SessionFacade {
    List<PunterSession> sessionList = new ArrayList<PunterSession>(10);
    private static SessionFacade instance;

    private SessionFacade() {

    }

    public static SessionFacade getInstance() {
        if (instance == null) {
            instance = new SessionFacade();
        }
        return instance;
    }

    public void removeSession(String sessionId) {
        PunterSession tempSession = new PunterSession(sessionId);
        int index = Collections.binarySearch(sessionList, tempSession);
        if (index != -1) {
            sessionList.remove(index);
        }
    }

    public PunterSession getSession(String sessionId) {
        Collections.sort(sessionList);
        PunterSession tempSession = new PunterSession(sessionId);
        int index = Collections.binarySearch(sessionList, tempSession);
        if (index == -1) {
            sessionList.add(tempSession);
            return tempSession;
        }
        return sessionList.get(index);
    }

    public PunterMessage getMessage(String sessionId) throws InterruptedException {
         return ((BlockingQueue<PunterMessage>)getSession(sessionId).getSession("queue")).take();
    }

    public void sendMessage(String sessionId,PunterMessage punterMessage) throws InterruptedException {
         ((BlockingQueue<PunterMessage>)getSession(sessionId).getSession("queue")).offer(punterMessage);
    }

}
