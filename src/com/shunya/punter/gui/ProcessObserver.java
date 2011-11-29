package com.shunya.punter.gui;

import com.shunya.punter.jpa.ProcessHistory;

public interface ProcessObserver{
void update(ProcessHistory ph);
void processCompleted();
}
