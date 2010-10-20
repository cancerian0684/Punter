package com.sapient.punter.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class AppSettings implements Serializable{
	private static final long serialVersionUID = 8652757533411927346L;
	private static AppSettings appSettings;
	public Dimension KBFrameDimension;
	public Point KBFrameLocation; 
	public Point PunterGuiFrameLocation; 
	private AppSettings(){
		KBFrameLocation=new Point(0, 0);
		PunterGuiFrameLocation=new Point(0, 0);
	}
	public static AppSettings getInstance(){
		if(appSettings==null){
			loadState();
			Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run() {
				super.run();
				saveState();
			}});
		}
		return appSettings;
	}
	private static void saveState(){
		  System.out.println("serializing the settings.");
		  try {
			  AppSettings appSettings=AppSettings.getInstance();
		      FileOutputStream fout = new FileOutputStream("punter.dat");
		      ObjectOutputStream oos = new ObjectOutputStream(fout);
		      oos.writeObject(appSettings);
		      oos.close();
		      }
		   catch (Exception e) { e.printStackTrace(); }

	}
	private static void loadState(){
		 try {
			    FileInputStream fin = new FileInputStream("punter.dat");
			    ObjectInputStream ois = new ObjectInputStream(fin);
			    appSettings =  (AppSettings) ois.readObject();
			    ois.close();
			    System.out.println("Settings loaded succesfully.");
			    }
			   catch (Exception e) { e.printStackTrace();
			   appSettings=new AppSettings();
			   }
	}
}
