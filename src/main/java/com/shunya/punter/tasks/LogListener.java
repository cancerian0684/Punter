package com.shunya.punter.tasks;

public interface LogListener {
    void log(String msg);

    void showLog();

    void disposeLogs();
}
