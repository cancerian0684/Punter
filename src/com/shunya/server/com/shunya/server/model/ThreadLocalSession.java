package com.shunya.server.com.shunya.server.model;

import javax.persistence.EntityManager;

public class ThreadLocalSession implements SessionCache{
    @Override
    public EntityManager getUnderlyingEntityManager() {
        return threadLocalJPASession.get();
    }
    private static class ThreadLocalJPASession extends ThreadLocal<EntityManager>{
        @Override
        protected EntityManager initialValue() {
            System.out.println("Creating new EntityManager. ");
            return JPASessionFactory.getInstance().getSession();
        }
    }
    public final ThreadLocalJPASession threadLocalJPASession = new ThreadLocalJPASession();

    public void set(EntityManager em){
        threadLocalJPASession.set(em);
    }
    public void clear() {
        threadLocalJPASession.get().close();
        threadLocalJPASession.remove();
        System.out.println("Destroyed EntityManager. ");
    }
}