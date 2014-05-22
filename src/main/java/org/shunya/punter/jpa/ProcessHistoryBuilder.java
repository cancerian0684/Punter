package org.shunya.punter.jpa;

import org.shunya.server.component.StaticDaoFacade;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProcessHistoryBuilder {
    public static ProcessHistory build(final ProcessData procDao, StaticDaoFacade staticDaoFacade) {
        List<TaskData> sortedTasksByProcessId = staticDaoFacade.getSortedTasksByProcessId(procDao.getId());
        final List<TaskHistory> thList = new ArrayList<>(10);
        final ProcessHistory processHistory = new ProcessHistory();
        processHistory.setName(procDao.getName());
        processHistory.setStartTime(new Date());
        processHistory.setProcess(procDao);
        for (TaskData taskDao : sortedTasksByProcessId) {
            TaskHistory taskHistory = new TaskHistory();
            taskHistory.setTask(taskDao);
            taskHistory.setProcessHistory(processHistory);
            taskHistory.setSequence(taskDao.getSequence());
            thList.add(taskHistory);
        }
        processHistory.setTaskHistoryList(thList);
        return processHistory;
    }
}
