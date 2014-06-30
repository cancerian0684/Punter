package org.shunya.punter.executors;

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
            }
        }
        return this;
    }

    public void shutdown(){
        pool.shutdownNow();
    }
}
