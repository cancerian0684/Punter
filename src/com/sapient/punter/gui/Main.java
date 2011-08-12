package com.sapient.punter.gui;

import java.awt.AWTException;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.UIManager;

import com.sapient.kb.gui.PunterKB;
import com.sapient.kb.jpa.StaticDaoFacade;
import com.sapient.punter.executors.ProcessExecutor;
import com.sapient.punter.utils.Launcher;
import com.sapient.punter.utils.StackWindow;

public class Main{
	private static BufferedImage currentImage;
	private static BufferedImage busyImage;
	private static BufferedImage dsctImage;
	private static BufferedImage idleImage;
	private static TrayIcon trayIcon;
	public static JFrame KBFrame;
	public static JFrame PunterGuiFrame;
	public static JFrame lastAccessed;
	private static Logger logger = Logger.getLogger(Main.class.getName());
	private Timer timer=new Timer(2000,new ActionListener(){
		public void actionPerformed(ActionEvent e) {
			setAppropriateTrayIcon();
		}

		private void setAppropriateTrayIcon() {
			BufferedImage requiredImage=null;
			if (isBusy()){
				requiredImage=busyImage;
				
			}else if(!isConnected()){
				requiredImage=dsctImage;
			}
			else {
				requiredImage=idleImage;
			}
			if(currentImage!=requiredImage){
				currentImage=requiredImage;
				trayIcon.setImage(currentImage);
				PunterGuiFrame.setIconImage(currentImage);
				KBFrame.setIconImage(currentImage);
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
	String response = JOptionPane.showInputDialog("Enter NT Logon ID",System.getProperty("user.name"));
	if(!response.isEmpty()){
		AppSettings.getInstance().setUsername(response.trim());
	}else{
		AppSettings.getInstance().setUsername(System.getProperty("user.name"));		
	}
	KBFrame=new JFrame("Knowledge Base");
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
			displayMsg("Personal Assistant has been minimized to System Tray",TrayIcon.MessageType.INFO);
		}
    });
    
    PunterGuiFrame=new JFrame("My Personal Assistant");
    PunterGuiFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    PunterGuiFrame.setContentPane(new PunterGUI());
    
    PunterGuiFrame.addWindowListener(new java.awt.event.WindowAdapter() {
	        		public void windowIconified(WindowEvent e) {
//	        			frame.dispose(); 
	        		 }
	        		public void windowClosing(WindowEvent e) {
	        			//setVisible(false);
	        			PunterGuiFrame.dispose(); 
	        			displayMsg("Assistant has been minimized to System Tray",TrayIcon.MessageType.INFO);
	        		}
	        });	 
    PunterGuiFrame.pack();
    if(AppSettings.getInstance().PunterGuiFrameLocation!=null)
    PunterGuiFrame.setLocation(AppSettings.getInstance().PunterGuiFrameLocation);
		Thread.UncaughtExceptionHandler handler = new StackWindow("Unhandled Exception", 500, 400);
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
	        }

	        public void mouseEntered(MouseEvent e) {
	        }

	        public void mouseExited(MouseEvent e) {
	        }

	        public void mousePressed(MouseEvent e) {
	        }

	        public void mouseReleased(MouseEvent e) {
	        }
	    };
	    Runtime rt = Runtime.getRuntime();
	    rt.addShutdownHook(new Thread() {
	      public void run() {
	    	timer.stop();
	    	logger.log(Level.INFO, "Exiting...");
	      }
	    });

	    ActionListener exitListener = new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            int option=JOptionPane.showConfirmDialog(PunterGuiFrame,"Exit My Assistant?","Confirm Exit", JOptionPane.OK_CANCEL_OPTION);
    			if(option==JOptionPane.OK_OPTION)
    			{
    				AppSettings.getInstance().KBFrameLocation=KBFrame.getLocation();
    				AppSettings.getInstance().setKBFrameDimension(KBFrame.getSize());
    				AppSettings.getInstance().PunterGuiFrameLocation=PunterGuiFrame.getLocation();
    				logger.log(Level.INFO, "Removing tray icon : "+KBFrame.getSize());
    				tray.remove(trayIcon);
    				Launcher.programQuit();
    			}
	        }
	    };
	            
	    PopupMenu popup = new PopupMenu();
	    MenuItem openPunterMenuItem = new MenuItem("My Assistant");
	    openPunterMenuItem.setFont(new Font("Tahoma", Font.BOLD, 12));
	    openPunterMenuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				PunterGuiFrame.setExtendedState(Frame.NORMAL);
				PunterGuiFrame.setVisible(true);
				lastAccessed=PunterGuiFrame;
			}});
	    popup.add(openPunterMenuItem);
	    
	    openPunterMenuItem = new MenuItem("Knowledge Base");
	    openPunterMenuItem.setFont(new Font("Tahoma", Font.BOLD, 12));
	    openPunterMenuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				KBFrame.setExtendedState(Frame.NORMAL);
				KBFrame.setVisible(true);
				lastAccessed=KBFrame;
			}});
	    popup.add(openPunterMenuItem);
	   
	    popup.addSeparator();
	    MenuItem defaultItem = new MenuItem("Exit");
	    defaultItem.addActionListener(exitListener);
	    popup.add(defaultItem);

	    trayIcon = new TrayIcon(idleImage, "My Assistant", popup);
	    trayIcon.setToolTip("My Assistant started.\nSapient Corp Pvt. Ltd.");
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
	        trayIcon.displayMessage("My Assistant", 
		            "Double click here to launch the Assistant.",
		            TrayIcon.MessageType.INFO);
	    } catch (AWTException e) {
	    	logger.log(Level.WARNING, "TrayIcon could not be added.");
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
		logger.log(Level.WARNING, "connection to server lost.");
		try{
			StaticDaoFacade.getInstance().makeConnection();
		}catch (Exception ee) {
			// TODO: handle exception
		}
		return false;
	}
}
public static void displayMsg(String msg,TrayIcon.MessageType msgType){
	if(trayIcon!=null){
		trayIcon.displayMessage("My Assistant", msg,msgType);
	}
}
public static void main(String[] args) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
        	try{
        		if(AppSettings.getInstance().isNimbusLookNFeel())
        			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        		else
        			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                Main main=new Main();
                }catch (Exception e) {
                	e.printStackTrace();
        		}
        }
    });
}
}
