package com.shunya.punter.jpa;

import com.shunya.kb.jpa.StaticDaoFacade;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mchan2
 * Date: 10/3/11
 * Time: 11:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProcessHistoryBuilder {
    public static ProcessHistory build(final ProcessData procDao, StaticDaoFacade staticDaoFacade) {
        List<TaskData> sortedTasksByProcessId = staticDaoFacade.getSortedTasksByProcessId(procDao.getId());
        final List<TaskHistory> thList = new ArrayList<>(10);
        final ProcessHistory processHistory = new ProcessHistory();
        processHistory.setName(procDao.getName());
        processHistory.setStartTime(new Date());
        processHistory.setProcess(procDao);
        processHistory.setTaskHistoryList(thList);
        for (TaskData taskDao : sortedTasksByProcessId) {
            TaskHistory taskHistory = new TaskHistory();
            taskHistory.setTask(taskDao);
            taskHistory.setProcessHistory(processHistory);
            taskHistory.setSequence(taskDao.getSequence());
            thList.add(taskHistory);
        }
        return processHistory;
    }
}
