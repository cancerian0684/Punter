package com.shunya.server.com.shunya.server.model;

import javax.persistence.EntityManager;

public class Context {
    EntityManager entityManager;

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}