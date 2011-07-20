package com.sapient.server;

import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Main {
	public static void main(String args[]) {
		try {
			String codebaseURI = new File("bin/").toURL().toURI().toString();
			System.out.println("Codebase is :" + codebaseURI);
			System.setProperty("java.rmi.server.codebase", codebaseURI);
			System.setProperty("java.rmi.server.hostname", findHostName());
			// System.setProperty("java.rmi.server.name","munishc-2k8");
			System.setProperty("java.security.policy", "policy.all");
			System.out.println("Killing the already running RMI Registry");
			Runtime.getRuntime().exec("taskkill /IM RMIREGISTRY.EXE");
			Thread.sleep(2000);
			System.out.println("Starting the rmi registry");
			//LocateRegistry.createRegistry(port)
			final Process proc = Runtime.getRuntime().exec("rmiregistry");
			Runtime.getRuntime().addShutdownHook(new Thread(){
				@Override
				public void run() {
					super.run();
					System.err.println("stopping RMI server.");
					proc.destroy();
					MultiCastResponder.getInstance().shutdown();
				}
			});
			Thread.sleep(1000);
			PunterSearch obj = new PunterSearchServer();
			PunterSearch stub = (PunterSearch) UnicastRemoteObject.exportObject(obj, 0);
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind("PunterSearch", stub);
			System.err.println("RMI Server ready");
			MultiCastResponder.getInstance();
			ServerSettings.getInstance();
			java.awt.Desktop.getDesktop().browse(new URI("http://localhost:8080/index.html")); 
			WebServer.main(new String[]{});
			System.err.println("Web Server ready");
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}
	
	private static String findHostName() {
		try {
			String hostname=InetAddress.getLocalHost().getHostName();
			System.err.println("Hostname : "+hostname);
			return hostname;
		} catch (UnknownHostException e) {
			return "localhost";
		}
	}
}
