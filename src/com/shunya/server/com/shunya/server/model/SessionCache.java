package com.shunya.server.com.shunya.server.model;

import javax.persistence.EntityManager;

public interface SessionCache {
    public EntityManager getUnderlyingEntityManager();
}
