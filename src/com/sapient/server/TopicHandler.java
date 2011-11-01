package com.sapient.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class TopicHandler {
    private static TopicHandler instance;
    Map<String, ArrayList<PunterSession>> topicSessionMap = new HashMap<String, ArrayList<PunterSession>>(10);

    private TopicHandler() {
    }

    public static TopicHandler getInstance() {
        if (instance == null) {
            instance = new TopicHandler();
        }
        return instance;
    }

    public synchronized void subscribe(PunterSession punterSession, String queue) {
        if (topicSessionMap.get(queue) == null) {
            topicSessionMap.put(queue, new ArrayList<PunterSession>());
        }
        topicSessionMap.get(queue).add(punterSession);
    }

    public synchronized void unSubscribe(PunterSession punterSession, String queue) {
        topicSessionMap.get(queue).remove(punterSession);
    }

    public synchronized void publishMessage(String sessionId, PunterMessage punterMessage, String topic) {
        ArrayList<PunterSession> arrayList = topicSessionMap.get(topic);
        for (PunterSession punterSession : arrayList) {
            if (!punterSession.getSessionId().equalsIgnoreCase(sessionId))
                punterSession.sendMessage(punterMessage);
        }
    }
}