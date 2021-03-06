package org.shunya.punter.gui;

import org.shunya.punter.jpa.TaskHistory;
import org.shunya.server.PunterProcessRunMessage;

import java.util.Map;
import java.util.concurrent.Future;

public interface TaskObserver {
    Future<Map> createAndRunProcessSync(PunterProcessRunMessage processRunMessage);

    void saveTaskHistory(TaskHistory taskHistory);

    void update(TaskHistory taskHistory);
}
