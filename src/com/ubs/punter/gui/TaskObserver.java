package com.ubs.punter.gui;

import com.ubs.punter.jpa.TaskHistory;

public interface TaskObserver {
public void updateTaskHistory(TaskHistory taskHistory);
}
