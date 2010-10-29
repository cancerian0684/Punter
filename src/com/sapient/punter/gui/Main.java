package com.sapient.punter.gui;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.apache.derby.drda.NetworkServerControl;

import com.sapient.kb.gui.PunterKB;
import com.sapient.kb.jpa.StaticDaoFacade;
import com.sapient.punter.executors.ProcessExecutor;
import com.sapient.punter.utils.Launcher;
import com.sapient.punter.utils.StackWindow;

public class Main{
	private static BufferedImage busyImage;
	private static BufferedImage dsctImage;
	private static BufferedImage idleImage;
	private static TrayIcon trayIcon;
	public static JFrame KBFrame;
	public static JFrame PunterGuiFrame;
	public static JFrame lastAccessed;
	
	private Timer timer=new Timer(2000,new ActionListener(){
		public void actionPerformed(ActionEvent e) {
			if (isBusy()){
				trayIcon.setImage(busyImage);
				PunterGuiFrame.setIconImage(busyImage);
				KBFrame.setIconImage(busyImage);
			}else if(!isConnected()){
				trayIcon.setImage(dsctImage);
			}
			else{
				PunterGuiFrame.setIconImage(idleImage);
				KBFrame.setIconImage(idleImage);
				trayIcon.setImage(idleImage);
			}
		}});
	public Main() {
		try {
			createAndShowGUI();
			timer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

private void createAndShowGUI() throws Exception {
	KBFrame=new JFrame("Punter KB");
	JFrame.setDefaultLookAndFeelDecorated(true);
	KBFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    KBFrame.setContentPane(new PunterKB());
    if(AppSettings.getInstance().KBFrameLocation!=null)
    KBFrame.setLocation(AppSettings.getInstance().KBFrameLocation);
    if(AppSettings.getInstance().getKBFrameDimension()!=null)
    KBFrame.setPreferredSize(AppSettings.getInstance().getKBFrameDimension());
    KBFrame.pack();
    KBFrame.addWindowListener(new java.awt.event.WindowAdapter() {
		public void windowIconified(WindowEvent e) {
			KBFrame.setVisible(false); 
		 }
		public void windowClosing(WindowEvent e) {
			KBFrame.setVisible(false); 
			displayMsg("Punter has been minimized to System Tray");
		}
    });	 
    
    PunterGuiFrame=new JFrame("My Punter");
    PunterGuiFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    PunterGuiFrame.setContentPane(new PunterGUI());
    
    PunterGuiFrame.addWindowListener(new java.awt.event.WindowAdapter() {
	        		public void windowIconified(WindowEvent e) {
//	        			frame.dispose(); 
	        		 }
	        		public void windowClosing(WindowEvent e) {
	        			//setVisible(false);
	        			PunterGuiFrame.dispose(); 
	        			displayMsg("Punter has been minimized to System Tray");
	        		}
	        });	 
    PunterGuiFrame.pack();
    if(AppSettings.getInstance().PunterGuiFrameLocation!=null)
    PunterGuiFrame.setLocation(AppSettings.getInstance().PunterGuiFrameLocation);
    Thread.UncaughtExceptionHandler handler =
         new StackWindow("Unhandled Exception", 500, 400);
       Thread.setDefaultUncaughtExceptionHandler(handler);
       
      /* 
       System.setOut( new PrintStream(
				new ConsoleOutputStream (new Document(), System.out), true));
		System.setErr( new PrintStream(
				new ConsoleOutputStream (wcw.getLogArea().getDocument (), null), true));*/
    /*try{
		AWTUtilities.setWindowOpacity(frame, 0.990f);
		Shape shape = null;
		shape =  new RoundRectangle2D.Float(0, 0, frame.getWidth(), frame.getHeight(), 30, 30);
        shape = new Ellipse2D.Float(0, 0, frame.getWidth(), frame.getHeight());
        AWTUtilities.setWindowShape(frame, shape);
	}catch(Exception e){};*/
   /* try {
		//invoke AWTUtilities.setWindowOpacity(win, 0.0f); using reflection so that earlier jdk's don't give erros
		Class awtutil = Class.forName("com.sun.awt.AWTUtilities");
		Method setWindowOpaque = awtutil.getMethod("setWindowOpacity", Window.class, float.class);
		setWindowOpaque.invoke(null, frame, (float)0.99);
		} catch (Exception ex) {
			ex.printStackTrace();
		}*/
    //Display the window.
	if (SystemTray.isSupported()) {
	    final SystemTray tray = SystemTray.getSystemTray();
        
		try {
        	busyImage = ImageIO.read(PunterGUI.class.getResource("/images/punter_busy.png"));
        	idleImage = ImageIO.read(PunterGUI.class.getResource("/images/punter_discnt.png"));
        	dsctImage = ImageIO.read(PunterGUI.class.getResource("/images/punter_idle.png"));
        	PunterGuiFrame.setIconImage(busyImage);
        	KBFrame.setIconImage(busyImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch(Exception e){
        	e.printStackTrace();
        }
	    MouseListener mouseListener = new MouseListener() {
	        public void mouseClicked(MouseEvent e) {
	        	if(e.getButton()==MouseEvent.BUTTON1){
	        		if(lastAccessed==null){
		        		lastAccessed=KBFrame;
		        	}
		        	lastAccessed.setExtendedState(Frame.NORMAL);
		        	lastAccessed.setVisible(true);
	        	}
//	            System.out.println("Tray Icon - Mouse clicked!");                 
	        }

	        public void mouseEntered(MouseEvent e) {
//	            System.out.println("Tray Icon - Mouse entered!");                 
	        }

	        public void mouseExited(MouseEvent e) {
	         //   System.out.println("Tray Icon - Mouse exited!");                 
	        }

	        public void mousePressed(MouseEvent e) {
	         //   System.out.println("Tray Icon - Mouse pressed!");                 
	        }

	        public void mouseReleased(MouseEvent e) {
	       //     System.out.println("Tray Icon - Mouse released!");                 
	        }
	    };
	    Runtime rt = Runtime.getRuntime();
	    System.err.println("Main: adding shutdown hook");
	    rt.addShutdownHook(new Thread() {
	      public void run() {
	    	timer.stop();
			System.out.println("Exiting...");
	      }
	    });

	    ActionListener exitListener = new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            int option=JOptionPane.showConfirmDialog(PunterGuiFrame,"Exit Punter?","Confirm Exit", JOptionPane.OK_CANCEL_OPTION);
    			if(option==JOptionPane.OK_OPTION)
    			{
    				AppSettings.getInstance().KBFrameLocation=KBFrame.getLocation();
    				AppSettings.getInstance().setKBFrameDimension(KBFrame.getSize());
    				AppSettings.getInstance().PunterGuiFrameLocation=PunterGuiFrame.getLocation();
    				System.out.println("Removing tray icon : "+KBFrame.getSize());
    				tray.remove(trayIcon);
    				Launcher.programQuit();
    				//dispose();
    				//System.exit(0);
    			}
	        }
	    };
	            
	    PopupMenu popup = new PopupMenu();
	    MenuItem openPunterMenuItem = new MenuItem("My Punter");
	    openPunterMenuItem.setFont(new Font("Tahoma", Font.BOLD, 12));
	    openPunterMenuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				PunterGuiFrame.setExtendedState(Frame.NORMAL);
				PunterGuiFrame.setVisible(true);
				lastAccessed=PunterGuiFrame;
			}});
	    popup.add(openPunterMenuItem);
	    
	    openPunterMenuItem = new MenuItem("My KB");
	    openPunterMenuItem.setFont(new Font("Tahoma", Font.BOLD, 12));
	    openPunterMenuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				KBFrame.setExtendedState(Frame.NORMAL);
				KBFrame.setVisible(true);
				lastAccessed=KBFrame;
			}});
	    popup.add(openPunterMenuItem);
	   
	  //  popup.add(new JSeparator());
	    MenuItem defaultItem = new MenuItem("Exit Punter");
	    defaultItem.addActionListener(exitListener);
	    popup.add(defaultItem);

	    trayIcon = new TrayIcon(idleImage, "My Punter", popup);
	    trayIcon.setToolTip("My Punter started.\nSapient Corp Pvt. Ltd.");
	    trayIcon.setImageAutoSize(true);
	   
	    ActionListener actionListener = new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	        	if(lastAccessed==null){
	        		lastAccessed=KBFrame;
	        	}
	        	lastAccessed.setExtendedState(Frame.NORMAL);
	        	lastAccessed.setVisible(true);
	        }
	    };
	            
	    trayIcon.setImageAutoSize(true);
	    trayIcon.addActionListener(actionListener);
	    trayIcon.addMouseListener(mouseListener);

	    try {
	        tray.add(trayIcon);
	        trayIcon.displayMessage("My Punter", 
		            "Double click here to launch the Punter.",
		            TrayIcon.MessageType.INFO);
	    } catch (AWTException e) {
	        System.err.println("TrayIcon could not be added.");
	    }

	} else {

	    //  System Tray is not supported

	}
}
public static boolean isBusy(){
	return ProcessExecutor.getInstance().isActive();
}
public static boolean isConnected(){
	try{
		StaticDaoFacade.getInstance().ping();
		return true;
	}catch (Exception e) {
		System.err.println("connection to server lost.");
		try{
			StaticDaoFacade.getInstance().makeConnection();
		}catch (Exception ee) {
			// TODO: handle exception
		}
		return false;
	}
}
public static void displayMsg(String msg){
	if(trayIcon!=null){
		trayIcon.displayMessage("My Punter", msg,TrayIcon.MessageType.INFO);
	}
}
public static void main(String[] args) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
        	try{
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                Main main=new Main();
                }catch (Exception e) {
                	e.printStackTrace();
        		}
        }
    });
}
}
