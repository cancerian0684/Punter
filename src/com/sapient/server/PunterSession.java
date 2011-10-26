package com.sapient.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PunterSession implements Comparable<String> {
    public PunterSession(String sessionId, String username) {
        this.sessionId = sessionId;
        this.username = username;
        this.myQueue = new LinkedBlockingQueue<PunterMessage>();
        this.sessionMap.put("queue", new LinkedBlockingQueue<PunterMessage>());
        this.age = 1000 * 60 * 10; //10 minutes
        this.lastAccessed = System.currentTimeMillis();
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUsername() {
        return username;
    }

    String username;
    String sessionId;
    Map<String, Object> sessionMap = new HashMap<String, Object>();
    BlockingQueue<PunterMessage> myQueue;
    long lastAccessed;
    long age;

    Object getObject(String key) {
        return sessionMap.get(key);
    }

    @Override
    public int compareTo(String o) {
        return o.compareTo(sessionId);
    }

    public boolean isExpired(){
        return age>(System.currentTimeMillis()-lastAccessed);
    }
    public void ping() {
        this.lastAccessed = System.currentTimeMillis();
    }
}
