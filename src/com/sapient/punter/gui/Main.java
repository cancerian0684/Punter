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

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.UIManager;

import com.sapient.punter.executors.ProcessExecutor;
import com.sapient.punter.utils.Launcher;
import com.sapient.punter.utils.StackWindow;

public class Main extends JFrame{
	private static BufferedImage busyImage;
	private static BufferedImage idleImage;
	private static TrayIcon trayIcon;
	public static Main main;
	private Timer timer=new Timer(2000,new ActionListener(){
		public void actionPerformed(ActionEvent e) {
			if (isBusy()){
				trayIcon.setImage(busyImage);
				main.setIconImage(busyImage);
			}else{
				main.setIconImage(idleImage);
				trayIcon.setImage(idleImage);
			}
		}});
	public Main() {
		super("My Punter");
		try {
			createAndShowGUI();
			main=this;
			timer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

private void createAndShowGUI() throws Exception {
    setDefaultLookAndFeelDecorated(true);
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

    PunterGUI newContentPane = new PunterGUI();
    newContentPane.setOpaque(true); //content panes must be opaque
    setContentPane(newContentPane);
    
	addWindowListener(
	        	new java.awt.event.WindowAdapter() {
	        		public void windowIconified(WindowEvent e) {
	        			//System.err.println("Iconifying window");
//	        			frame.dispose(); 
	        		 }
	        		public void windowClosing(WindowEvent e) {
	        			//setVisible(false);
	        			dispose(); 
	        			displayMsg("Punter has been minimized to System Tray");
	        		}
	        });	 
    
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
        	idleImage = ImageIO.read(PunterGUI.class.getResource("/images/punter_idle.png"));
        	setIconImage(busyImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch(Exception e){
        	e.printStackTrace();
        }
	    MouseListener mouseListener = new MouseListener() {
	        public void mouseClicked(MouseEvent e) {
	        	if(e.getButton()==MouseEvent.BUTTON1){
	        	setExtendedState(Frame.NORMAL);
	        	setVisible(true);
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
	        System.out.println("Running shutdown hook");
			System.out.println("Exiting...");
	      }
	    });

	    ActionListener exitListener = new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            int option=JOptionPane.showConfirmDialog(Main.this,"Exit Punter?","Confirm Exit", JOptionPane.OK_CANCEL_OPTION);
    			if(option==JOptionPane.OK_OPTION)
    			{
    				System.out.println("Removing tray icon");
    				tray.remove(trayIcon);
    				Launcher.programQuit();
    				//dispose();
    				//System.exit(0);
    			}
	        }
	    };
	            
	    PopupMenu popup = new PopupMenu();
	    MenuItem defaultItem1 = new MenuItem("Open Punter");
	    defaultItem1.setFont(new Font("Tahoma", Font.BOLD, 12));
	    defaultItem1.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				setExtendedState(Frame.NORMAL);
				setVisible(true);
			}});
	    popup.add(defaultItem1);
	   
	  //  popup.add(new JSeparator());
	    MenuItem defaultItem = new MenuItem("Exit Punter");
	    defaultItem.addActionListener(exitListener);
	    popup.add(defaultItem);

	    trayIcon = new TrayIcon(idleImage, "My Punter", popup);
	    trayIcon.setToolTip("My Punter started.\nSapient Corp Pvt. Ltd.");
	    trayIcon.setImageAutoSize(true);
	   
	    ActionListener actionListener = new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	        		setExtendedState(Frame.NORMAL);
	            	setVisible(true);
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
    pack();
    setVisible(true);
}
public static boolean isBusy(){
	return ProcessExecutor.getInstance().isActive();
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
