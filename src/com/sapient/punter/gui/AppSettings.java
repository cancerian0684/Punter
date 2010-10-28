package com.sapient.punter.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import javax.jnlp.BasicService;
import javax.jnlp.FileContents;
import javax.jnlp.PersistenceService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;

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
		  AppSettings appSettings=AppSettings.getInstance();
		  try {
			  PersistenceService ps; 
			  BasicService bs; 
			  ps = (PersistenceService)ServiceManager.lookup("javax.jnlp.PersistenceService"); 
			  bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService"); 
			  URL codebase = bs.getCodeBase();
			  FileContents fc = ps.get(codebase);
			  ObjectOutputStream oos = new ObjectOutputStream(fc.getOutputStream(true));
			  oos.writeObject( appSettings );
			  oos.flush();
			  oos.close();
		      }
		   catch (Exception e) { e.printStackTrace();
		   	  try{
			   	  FileOutputStream fout = new FileOutputStream("punter.dat");
			      ObjectOutputStream oos = new ObjectOutputStream(fout);
			      oos.writeObject(appSettings);
			      oos.close();
		   	  }catch (Exception ee) {
		   		  ee.printStackTrace();
			}
		   }

	}
	private static void loadState(){
		try {
			  PersistenceService ps; 
			  BasicService bs; 
			  ps = (PersistenceService)ServiceManager.lookup("javax.jnlp.PersistenceService"); 
			  bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService"); 
			  URL codebase = bs.getCodeBase();
			  FileContents settings = null;
			  settings = ps.get(codebase);
			  ObjectInputStream ois = new ObjectInputStream(settings.getInputStream() );
			  appSettings =  (AppSettings) ois.readObject();
			  ois.close();
		      }
 			catch(FileNotFoundException fnfe) {
 			try {
			  PersistenceService ps; 
			  BasicService bs; 
			  ps = (PersistenceService)ServiceManager.lookup("javax.jnlp.PersistenceService"); 
			  bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService"); 
			  URL codebase = bs.getCodeBase();
			  long size = ps.create(codebase, 64000);
			  System.out.println( "Cache created - size: " + size );
			 } catch(MalformedURLException murle) {
			 System.err.println( "Application codebase is not a valid URL?!" );
			 murle.printStackTrace();
			} catch(IOException ioe) {
				ioe.printStackTrace();
			} catch (UnavailableServiceException e) {
				e.printStackTrace();
				 try {
					    FileInputStream fin = new FileInputStream("punter.dat");
					    ObjectInputStream ois = new ObjectInputStream(fin);
					    appSettings =  (AppSettings) ois.readObject();
					    ois.close();
					    System.out.println("Settings loaded succesfully.");
					    }
					   catch (Exception ee) { ee.printStackTrace();
					   appSettings=new AppSettings();
					   }
			}
			 appSettings=new AppSettings();
 			} 
 			 catch (Exception ex) { 
				   ex.printStackTrace(); 
				   appSettings=new AppSettings();
			   }
 			
	}
}
