package com.shunya.punter.executors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JobExecutor {
    ExecutorService executorService = Executors.newCachedThreadPool();

    public void execute(Stream<JobConfig> jobConfigStream) {
        final Map<Integer, List<JobConfig>> collect = jobConfigStream.collect(Collectors.groupingBy(JobConfig::getSequence));
        TreeMap<Integer, List<JobConfig>> treeMap = new TreeMap(collect);
        treeMap.forEach((t, u) -> {
            if (u.size() > 1) {
                System.out.println("Running in parallel");
                List<Future> futures = new ArrayList<>();
                u.forEach(k -> {
                    futures.add(executorService.submit(() -> executeTask(k)));
                });
                futures.forEach(f -> {
                    try {
                        f.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                System.out.println("Running Sequentially");
                executeTask(u.get(0));
            }
        });
//        System.out.println("collect = " + treeMap);
    }

    public void executeTask(JobConfig k) {
        System.out.println(k.toString());
    }

    public void shutdown() {
        if (!executorService.isTerminated()) {
            executorService.shutdownNow();
        }
    }
}
