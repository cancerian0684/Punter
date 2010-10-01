package com.sapient.punter.gui;

import com.sapient.punter.jpa.TaskHistory;

public interface TaskObserver {
public void saveTaskHistory(TaskHistory taskHistory);
}
