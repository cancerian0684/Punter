package org.shunya.server.component;

import org.shunya.kb.model.AccessEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AccessCountEventListener {
    private final Logger logger = LoggerFactory.getLogger(AccessCountEventListener.class);

    @Autowired
    DBService dbService;

    @Async
    @EventListener
    public void handleAccessEvent(AccessEvent event) {
        logger.info("Handling AccessEvent");
        dbService.incrementCounter(event.getEntityName(), event.getEntityId());
    }
}
