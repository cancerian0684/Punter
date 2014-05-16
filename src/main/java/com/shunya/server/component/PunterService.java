package com.shunya.server.component;

import com.shunya.punter.jpa.TaskData;
import com.shunya.punter.tasks.Tasks;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PunterService {
    final Logger logger = LoggerFactory.getLogger(PunterService.class);
    @Autowired
    private StaticDaoFacade service;

    public Map<String, Object> runTask(TaskData taskData){
        System.out.println("task = " + taskData);
        Map<String, Object> resultsMap = new HashMap<>();
        Tasks task = Tasks.getTask(taskData);
        try {
            task.setTaskDao(taskData);
            task.setHosts(null);
            task.setSessionMap(resultsMap);
            task.beforeTaskStart();
            task.execute();
            task.afterTaskFinish();
            resultsMap.put("logs", task.getMemoryLogs());
            resultsMap.put("status", true);
        } catch (Exception e) {
            e.printStackTrace();
            resultsMap.put("status", false);
            resultsMap.put("error", Util.getExceptionSummary(e));
        }
        return resultsMap;
    }

}
