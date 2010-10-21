package com.sapient.server;

import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class PunterSearchServer implements PunterSearch {
public static void main(String args[]) {
        try {
        	String codebaseURI = new File("bin/").toURL().toURI().toString();
			System.out.println("Codebase is :" + codebaseURI);
			System.setProperty("java.rmi.server.codebase", codebaseURI);
			System.setProperty("java.rmi.server.hostname", "localhost");
			// System.setProperty("java.rmi.server.name","munishc-2k8");
			System.setProperty("java.security.policy", "policy.all");
			// Process p =
			System.out.println("Killing the already running RMI Registry");
		    Runtime.getRuntime().exec("taskkill /IM RMIREGISTRY.EXE");
		    Thread.sleep(2000);
			System.out.println("Starting the rmi registry");
			Runtime.getRuntime().exec("rmiregistry");
			Thread.sleep(1000);
        	PunterSearchServer obj = new PunterSearchServer();
            PunterSearch stub = (PunterSearch) UnicastRemoteObject.exportObject(obj,0);
            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("PunterSearch", stub);

            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}