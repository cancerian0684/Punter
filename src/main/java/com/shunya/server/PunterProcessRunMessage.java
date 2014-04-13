package com.shunya.server;

import java.util.HashMap;
import java.util.Map;

public class PunterProcessRunMessage extends PunterMessage {
    private long processId;
    private String hostname;
    private Map<String,String> params=new HashMap<String, String>();

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
}
