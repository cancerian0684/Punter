package org.shunya.punter.gui;

import org.shunya.punter.jpa.TaskHistory;

import java.util.Comparator;

public class TaskHistorySeqComparator implements Comparator{
	@Override
    public int compare(Object th1, Object th2){
		int seq1=((TaskHistory)th1).getSequence();
		int seq2=((TaskHistory)th2).getSequence();
        if(seq1 > seq2)
            return 1;
        else if(seq1 < seq2)
            return -1;
        else
            return 0;    
    }
}
