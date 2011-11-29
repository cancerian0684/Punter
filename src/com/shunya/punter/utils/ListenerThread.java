package com.shunya.punter.utils;

import java.awt.TrayIcon;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.shunya.punter.gui.Main;

public class ListenerThread extends Thread {
   public void run() {
      try {
    	
         ServerSocket server = new ServerSocket(Launcher.socketPort);
         while (true) {
            System.out.println("about to listen");
            Socket socket = server.accept();   
            Main.displayMsg("I am already here to serve you. why u need a new Instance ?",TrayIcon.MessageType.INFO);
            System.out.println("opened socket from client");
            InputStream iStream = socket.getInputStream();
            int length = iStream.read();
            byte[] bytes = new byte[length];
            iStream.read(bytes);
            String className = new String(bytes);
            if(className.equalsIgnoreCase("EXIT")){
            	System.err.println("Got an Exit request from a new instance of the program");
            	iStream.close();
            	server.close();
            	System.exit(0);
            }
            else if(className.equalsIgnoreCase("NULL")){
            	Main.displayMsg("Double click here to launch My Punter.",TrayIcon.MessageType.INFO);
            	iStream.close();
            }
            else
            Launcher.invokeMethod(className);
         }
      } catch (IOException e) {
         e.printStackTrace();
         System.out.println("Failed to start");
      } 
   }
}
