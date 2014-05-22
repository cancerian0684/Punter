package org.shunya.punter.gui;

import org.shunya.punter.jpa.ProcessHistory;

public interface ProcessObserver{
void update(ProcessHistory ph);
void processCompleted();
}
