package com.sapient.server;

import java.util.*;
import java.util.concurrent.BlockingQueue;

public class SessionFacade {
    Map<String, PunterSession> sessionMap = new HashMap<String, PunterSession>(10);
    TopicHandler topicHandler = TopicHandler.getInstance();
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
        topicHandler.unSubscribe(sessionMap.get(sessionId), "global");
        topicHandler.unSubscribe(sessionMap.get(sessionId), sessionMap.get(sessionId).getUsername());
        sessionMap.remove(sessionId);
    }

    public String getSession(String username) {
        PunterSession punterSession = new PunterSession(IdGenerator.getInstance().generateId(16), username);
        sessionMap.put(punterSession.getSessionId(), punterSession);
        topicHandler.subscribe(punterSession, "global");
        topicHandler.subscribe(punterSession, username);
        return punterSession.getSessionId();
    }

    public PunterMessage getMessage(String sessionId) throws InterruptedException {
        return ((BlockingQueue<PunterMessage>) sessionMap.get(sessionId).getObject("queue")).take();
    }

    public void sendMessage(String sessionId, PunterMessage punterMessage) throws InterruptedException {
        ((BlockingQueue<PunterMessage>) sessionMap.get(sessionId).getObject("queue")).offer(punterMessage);
    }

    public void sendMessage(String sessionId, PunterMessage punterMessage, String topic) {
        topicHandler.publishMessage(sessionId,punterMessage, topic);
    }

    public void ping(String sessionId) {
        sessionMap.get(sessionId).ping();
    }
}