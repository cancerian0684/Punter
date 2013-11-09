package com.shunya.server.model;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class JPATransatomatic implements Transatomatic {
    private ThreadLocalSession threadLocalSession;

    public JPATransatomatic(ThreadLocalSession threadLocalSession){
        this.threadLocalSession = threadLocalSession;
    }
    @Override
    public void run(UnitOfWork unitOfWork) {
        final EntityManager em = threadLocalSession.getUnderlyingEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            unitOfWork.run();
            tx.commit();
        } finally {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            threadLocalSession.clear();
        }
    }
}
