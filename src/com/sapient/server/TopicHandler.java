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

    public void subscribe(PunterSession punterSession, String queue) {
        if (topicSessionMap.get(queue) == null) {
            topicSessionMap.put(queue, new ArrayList<PunterSession>());
        }
        topicSessionMap.get(queue).add(punterSession);
    }

    public void unSubscribe(PunterSession punterSession, String queue) {
        topicSessionMap.get(queue).remove(punterSession);
    }

    public void publishMessage(String sessionId, PunterMessage punterMessage, String topic) {
        ArrayList<PunterSession> arrayList = topicSessionMap.get(topic);
        for (PunterSession punterSession : arrayList) {
            if (!punterSession.getSessionId().equalsIgnoreCase(sessionId))
                ((BlockingQueue<PunterMessage>) punterSession.getObject("queue")).offer(punterMessage);
        }
    }

}
