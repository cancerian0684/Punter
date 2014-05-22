package org.shunya.punter.executors;

import java.util.Map;

public interface ResultProcessor {
    void process(Map<String, Object> resultsMap);
}
