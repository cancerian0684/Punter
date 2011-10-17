package com.sapient.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PunterSession implements Comparable<PunterSession>{
    public PunterSession(String sessionId) {
        this.sessionId = sessionId;
        this.sessionMap.put("queue", new LinkedBlockingQueue<PunterMessage>());
    }

    public String getSessionId() {
        return sessionId;
    }

    String sessionId;
    Map<String,Object> sessionMap=new HashMap<String, Object>();

    Object getSession(String key){
        return sessionMap.get(key);
    }

    @Override
    public int compareTo(PunterSession o) {
        return o.getSessionId().compareTo(sessionId);
    }
}
