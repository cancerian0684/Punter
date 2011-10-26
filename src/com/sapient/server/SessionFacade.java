package com.sapient.server;

import java.util.*;
import java.util.concurrent.BlockingQueue;

public class SessionFacade {
    Map<String,PunterSession> sessionMap =new HashMap<String, PunterSession>(10);
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
        sessionMap.remove(sessionId);
    }

    public String getSession(String username) {
        PunterSession punterSession = new PunterSession(IdGenerator.getInstance().generateId(256), username);
        sessionMap.put(punterSession.getSessionId(),punterSession);
        return punterSession.getSessionId();
    }

    public PunterMessage getMessage(String sessionId) throws InterruptedException {
         return ((BlockingQueue<PunterMessage>)sessionMap.get(sessionId).getObject("queue")).take();
    }

    public void sendMessage(String sessionId,PunterMessage punterMessage) throws InterruptedException {
         ((BlockingQueue<PunterMessage>)sessionMap.get(sessionId).getObject("queue")).offer(punterMessage);
    }
}