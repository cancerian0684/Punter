package com.shunya.server.model;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class JPATransatomatic implements Transatomatic {
    private ThreadLocalSession threadLocalSession;

    public JPATransatomatic(ThreadLocalSession threadLocalSession) {
        this.threadLocalSession = threadLocalSession;
    }

    @Override
    public void run(UnitOfWork unitOfWork) {
        final Session em = threadLocalSession.getUnderlyingEntityManager();
        Transaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            unitOfWork.run();
            if (tx != null && tx.isActive()) {
                tx.commit();
            }
        } finally {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            threadLocalSession.clear();
        }
    }
}
