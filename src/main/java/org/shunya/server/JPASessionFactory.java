package org.shunya.server;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.hibernate.service.ServiceRegistry;
import org.shunya.punter.jpa.ProcessHistory;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class JPASessionFactory {
    private final SessionFactory ourSessionFactory;
    private final ServiceRegistry serviceRegistry;

    public JPASessionFactory() {
        try {
            Configuration configuration = new Configuration();
            configuration.configure();
            serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
            ourSessionFactory = configuration.buildSessionFactory(serviceRegistry);
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public Session getSession() throws HibernateException {
        return ourSessionFactory.getCurrentSession();
    }

    public static void main(final String[] args) throws Exception {
        JPASessionFactory jpaSessionFactory = new JPASessionFactory();
        final Session session = jpaSessionFactory.getSession();
        try {
           /* System.out.println("querying all the managed entities...");
            final Map metadataMap = session.getSessionFactory().getAllClassMetadata();
            for (Object key : metadataMap.keySet()) {
                final ClassMetadata classMetadata = (ClassMetadata) metadataMap.get(key);
                final String entityName = classMetadata.getEntityName();
                final Query query = session.createQuery("from " + entityName);
                System.out.println("executing: " + query.getQueryString());
                for (Object o : query.list()) {
                    System.out.println("  " + o);
                }
            }*/

            try {
                Calendar cal = GregorianCalendar.getInstance();
                cal.set(cal.DATE, cal.get(cal.DATE) - 10);
                List<ProcessHistory> processHistoryList = session.createCriteria(ProcessHistory.class).setMaxResults(100).add(Restrictions.lt("startTime", cal.getTime())).list();
                for (ProcessHistory processHistory : processHistoryList) {
                    ProcessHistory ph = (ProcessHistory) session.get(ProcessHistory.class, processHistory.getId());
                    session.delete(ph);
                    System.out.println("Removed : " + ph.getId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            session.close();
        }
    }
}
