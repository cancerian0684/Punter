package org.shunya.server;

import org.hibernate.*;

public class JPATransatomatic implements Transatomatic {
    private SessionFactory sessionFactory;

    public JPATransatomatic(SessionFactory jpaSessionFactory) {
        this.sessionFactory = jpaSessionFactory;
    }

    @Override
    public void run(UnitOfWork unitOfWork) {
        final Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.getTransaction();
            tx.begin();
            session.setFlushMode(FlushMode.AUTO);
            session.setCacheMode(CacheMode.IGNORE);
            unitOfWork.run(session);
            session.flush();
            session.clear();
            if (tx != null && tx.isActive()) {
                tx.commit();
            }
        } finally {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            session.close();
        }
    }
}
