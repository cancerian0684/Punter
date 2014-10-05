package org.shunya.punter.executors;

import org.shunya.punter.utils.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ParallelTaskRunner {
    final ExecutorService pool = Executors.newFixedThreadPool(3);
    final ExecutorCompletionService<Map<String,Object>> completionService = new ExecutorCompletionService<>(pool);

    public ParallelTaskRunner execute(List<Callable> tasks, ResultProcessor resultProcessor) throws InterruptedException {
        for (final Callable task : tasks) {
            completionService.submit(task);
        }

        for(int i = 0; i < tasks.size(); ++i) {
            final Future<Map<String, Object>> future = completionService.take();
            try {
                final Map<String, Object> resultsMap = future.get();
                resultProcessor.process(resultsMap);
            } catch (ExecutionException e) {
                e.printStackTrace();
                final Map<String, Object> resultsMap = new HashMap<>();
                resultsMap.put("status", false);
                resultsMap.put("logs", StringUtils.getExceptionStackTrace(e));
                resultProcessor.process(resultsMap);
            }
        }
        return this;
    }

    public void shutdown(){
        pool.shutdownNow();
    }
}
