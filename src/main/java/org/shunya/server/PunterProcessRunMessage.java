package org.shunya.server;

import java.util.HashMap;
import java.util.Map;

public class PunterProcessRunMessage extends PunterMessage {
    private long processId;
    private String hostname;
    private Map<String, String> params = new HashMap<String, String>();
    private Map<String, Object> results = new HashMap<>();
    private boolean done = false;

    public long getProcessId() {
        return processId;
    }

    public void setProcessId(long processId) {
        this.processId = processId;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public synchronized Map<String, Object> get() throws InterruptedException {
        while (!done)
            wait();
        return results;
    }

    public Map<String, Object> getResults() {
        return results;
    }

    public synchronized void setResults(Map<String, Object> results) {
        this.results = results;
    }

    public synchronized void markDone(){
        done = true;
        notifyAll();
    }
}
