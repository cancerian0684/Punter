package com.sapient.punter.utils;

import java.lang.reflect.*;
import java.net.*;
import java.io.*;

import javax.swing.JOptionPane;

public class Launcher {
   static final int socketPort = 9876;
   public void launch(String className) {
      System.out.println("Trying to launch:"+className);
      Socket s = findService();
      if (s != null) {
    	  Object[] options = { "Replace Instance", "Cancel Launch" };
		  int n = JOptionPane.showOptionDialog(null,
				    "An instance already running in system. Want to ?",
				    "Instance Found !",
				    JOptionPane.YES_NO_OPTION,
				    JOptionPane.QUESTION_MESSAGE,
				    null,     //do not use a custom Icon
				    options,  //the titles of buttons
				    options[0]); //default button title

         System.out.println("found running service");
         if(n==JOptionPane.YES_OPTION){
         try {
            OutputStream oStream = s.getOutputStream();
          //  byte[] bytes = className.getBytes();
            byte[] bytes ="EXIT".getBytes();
            oStream.write(bytes.length);
            oStream.write(bytes);
            oStream.flush();
            oStream.close();
         } catch (IOException e) {
            System.out.println("Couldn't talk to service");
         }
         System.out.println("Starting new service");
         try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
         Launcher.go(className);
         Thread listener = new ListenerThread();
         listener.start();
         System.out.println("started service listener");
         }
         else{
        	 try {
                 OutputStream oStream = s.getOutputStream();
               //  byte[] bytes = className.getBytes();
                 byte[] bytes ="NULL".getBytes();
                 oStream.write(bytes.length);
                 oStream.write(bytes);
                 oStream.flush();
                 oStream.close();
              } catch (IOException e) {
                 System.out.println("Couldn't talk to service");
              }
        	 System.exit(0);
         }
      } else {
         System.out.println("Starting new service");
         Launcher.go(className);
         Thread listener = new ListenerThread();
         listener.start();
         System.out.println("started service listener");
      }
   }

   protected Socket findService() {
      try {
         Socket s = new Socket(InetAddress.getLocalHost(),
                               socketPort);
         return s;
      } catch (IOException e) {
         // couldn't find a service provider
         return null;
      }
   }

   public static synchronized void go(final String className) {
      System.out.println("running a " + className);
      Thread thread = new Thread() {
         public void run() {
            try {
               Class clazz = Class.forName(className);
               Class[] argsTypes = {String[].class};
               Object[] args = {new String[0]};
               Method method = clazz.getMethod("main", argsTypes);
               method.invoke(clazz, args);
            } catch (Exception e) {
               System.out.println("coudn't run the :" + className);
               e.printStackTrace();
               System.out.println(e.getMessage());
            }
         }
      }; // end thread sub-class
      thread.start();
      runningPrograms++;
   }

   static int runningPrograms = 0;

   public static void programQuit() {
      runningPrograms--;
      if (runningPrograms <= 0) {
         System.exit(0);
      }
   }

   public static void main(String[] args) {
      Launcher l = new Launcher();
      l.launch("com.sapient.punter.gui.PunterGUI");
   }
}
