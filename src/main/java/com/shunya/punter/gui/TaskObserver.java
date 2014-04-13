package com.shunya.punter.gui;

import com.shunya.punter.jpa.TaskHistory;

public interface TaskObserver {
public void saveTaskHistory(TaskHistory taskHistory);
}
