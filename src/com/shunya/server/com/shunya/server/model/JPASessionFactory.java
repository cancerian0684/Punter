package com.shunya.server.com.shunya.server.model;

import org.apache.derby.drda.NetworkServerControl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.net.InetAddress;
import java.util.Properties;

public class JPASessionFactory {
    private static JPASessionFactory instance;
    private EntityManagerFactory emf;

    private JPASessionFactory() {
        try {
            Properties properties = System.getProperties();
            properties.put("derby.system.home", ".");
            final NetworkServerControl serverControl = new NetworkServerControl(InetAddress.getByName("localhost"), 1527);
            try {
                serverControl.start(null);
                serverControl.logConnections(true);
                System.err.println(serverControl.getRuntimeInfo());
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.currentThread();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        emf.close();
                        System.out.println("Shutting down DB server.");
                        serverControl.shutdown();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            emf = Persistence.createEntityManagerFactory("punter", properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static JPASessionFactory getInstance() {
        if (instance == null) {
            instance = new JPASessionFactory();
        }
        return instance;
    }

    public EntityManager getSession() {
        return emf.createEntityManager();
    }
}
