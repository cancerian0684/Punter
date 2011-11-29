package com.sapient.punter.gui;

import com.sapient.punter.jpa.ProcessHistory;

public interface ProcessObserver{
void update(ProcessHistory ph);
void processCompleted();
}
