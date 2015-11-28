package org.shunya.punter.tasks;

import ch.qos.logback.classic.Level;

public interface LogListener {
    void log(String msg, Level level);

    void showLog();

    void disposeLogs();
}
