package com.sapient.server;

public class PunterProcessRunMessage extends PunterMessage {
    private long processId;
    private String hostname;

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
}
