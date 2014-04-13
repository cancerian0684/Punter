package com.shunya.server.model;

import org.hibernate.Session;

public interface SessionCache {
    public Session getUnderlyingEntityManager();
}
