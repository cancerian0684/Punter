package com.shunya.server;

import com.shunya.punter.gui.SingleInstanceFileLock;
import com.shunya.server.model.JPATransatomatic;
import com.shunya.server.model.SessionCache;
import com.shunya.server.model.ThreadLocalSession;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Main {
    private SingleInstanceFileLock singleInstanceFileLock;
    private StaticDaoFacade staticDaoFacade;
    private SessionFacade sessionFacade;
    private SessionCache sessionCache;
    private JPATransatomatic transatomatic;
    private PunterHttpServer punterHttpServer;
    private ServerSettings serverSettings;
    private ServerContext context;

    Main() {
        singleInstanceFileLock = new SingleInstanceFileLock("PunterServer.lock");
        sessionFacade = SessionFacade.getInstance();
        sessionCache = new ThreadLocalSession();
        transatomatic = new JPATransatomatic((ThreadLocalSession) sessionCache);
        serverSettings = new ServerSettings();
        staticDaoFacade = new StaticDaoFacade(sessionCache, transatomatic);
        serverSettings.setStaticDaoFacade(staticDaoFacade);
        staticDaoFacade.setSettings(serverSettings);
        context = new ServerContext(staticDaoFacade, sessionFacade, sessionCache, transatomatic, serverSettings);
        punterHttpServer = new PunterHttpServer(context);
    }

    public void startServer() {
        try {
            if (singleInstanceFileLock.checkIfAlreadyRunning())
                System.exit(1);
            String codebaseURI = new File("bin/").toURL().toURI().toString();
            System.out.println("Codebase is :" + codebaseURI);
            System.setProperty("java.rmi.server.codebase", codebaseURI);
            System.setProperty("java.rmi.server.hostname", findHostName());
            System.setProperty("java.security.policy", "policy.all");

//            System.out.println("Killing the already running RMI Registry");
//            Runtime.getRuntime().exec("taskkill /IM RMIREGISTRY.EXE");
//            Thread.sleep(2000);
            System.out.println("Starting the rmi registry");
            final Process proc = Runtime.getRuntime().exec("rmiregistry -Djava.rmi.server.useCodebaseOnly=false 2020");
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    super.run();
                    System.err.println("stopping RMI server.");
                    proc.destroy();
                    MultiCastResponder.getInstance().shutdown();
                }
            });
            Thread.sleep(1000);
            PunterSearch obj = new PunterSearchServer(staticDaoFacade, sessionFacade, serverSettings);
//            obj.getProcess(1L);
            PunterSearch stub = (PunterSearch) UnicastRemoteObject.exportObject(obj, 0);
            Registry registry = LocateRegistry.createRegistry(2020);
            registry.rebind("PunterSearch", stub);
            System.err.println("RMI Server ready");
            MultiCastResponder.getInstance();
            punterHttpServer.start();
            java.awt.Desktop.getDesktop().browse(new URI("http://localhost:8080/index.html"));
            System.err.println("Web Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws IOException {
        long t1= System.currentTimeMillis();
        Main main = new Main();
        main.startServer();
        long t2= System.currentTimeMillis();
        System.out.println("Server up in ["+(t2-t1)+"] ms, Press Enter to terminate");
        System.in.read();
        System.exit(0);
    }

    private String findHostName() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            System.err.println("Hostname : " + hostname);
            return hostname;
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }
}
