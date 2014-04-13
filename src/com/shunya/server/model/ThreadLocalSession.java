package com.shunya.server.model;

import org.hibernate.Session;

public class ThreadLocalSession implements SessionCache {
    @Override
    public Session getUnderlyingEntityManager() {
        return threadLocalJPASession.get();
    }

    private static class ThreadLocalJPASession extends ThreadLocal<Session> {
        @Override
        protected Session initialValue() {
            return com.shunya.server.model.JPASessionFactory.getSession();
        }
    }

    public final ThreadLocalJPASession threadLocalJPASession = new ThreadLocalJPASession();

    public void set(Session em) {
        threadLocalJPASession.set(em);
    }

    public void clear() {
        threadLocalJPASession.get().close();
        threadLocalJPASession.remove();
    }
}