package com.shunya.server.model;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class JPATransatomatic implements Transatomatic {
    private JPASessionFactory jpaSessionFactory;

    public JPATransatomatic(JPASessionFactory jpaSessionFactory) {
        this.jpaSessionFactory = jpaSessionFactory;
    }

    @Override
    public void run(UnitOfWork unitOfWork) {
        final Session session = jpaSessionFactory.getSession();
        Transaction tx = null;
        try {
            tx = session.getTransaction();
            tx.begin();
            unitOfWork.run(session);
            if (tx != null && tx.isActive()) {
                tx.commit();
            }
        } finally {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
//            session.close();
        }
    }
}
