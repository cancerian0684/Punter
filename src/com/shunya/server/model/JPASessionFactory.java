package com.shunya.server.model;

import org.h2.tools.Server;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;

public class JPASessionFactory {
    private static JPASessionFactory instance;
    private EntityManagerFactory emf;

    private JPASessionFactory() {
        try {
            final Server server = Server.createTcpServer().start();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        emf.close();
                        System.out.println("Shutting down DB server.");
                        server.stop();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            emf = Persistence.createEntityManagerFactory("punter", new HashMap());
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
