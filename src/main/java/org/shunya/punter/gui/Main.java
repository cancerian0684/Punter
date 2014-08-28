package org.shunya.punter.gui;

import org.shunya.kb.gui.PunterKB;
import org.shunya.punter.executors.ProcessExecutor;
import org.shunya.punter.utils.GlobalHotKeyListener;
import org.shunya.punter.utils.JavaScreenCapture;
import org.shunya.punter.utils.Launcher;
import org.shunya.punter.utils.StackWindow;
import org.shunya.server.component.PunterService;
import org.shunya.server.component.StaticDaoFacade;
import org.springframework.beans.factory.annotation.Autowired;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    @Autowired
    private StaticDaoFacade staticDaoFacade;
    @Autowired
    private PunterService punterService;
    private static BufferedImage currentImage;
    private static BufferedImage busyImage;
    private static BufferedImage dsctImage;
    private static BufferedImage idleImage;
    private static TrayIcon trayIcon;
    public static JFrame KBFrame;
    public static JFrame PunterGuiFrame;
    public static JFrame lastAccessed;
    private static Logger logger = Logger.getLogger(Main.class.getName());
    private GlobalHotKeyListener globalHotKeyListener;

    private SingleInstanceFileLock singleInstanceFileLock = new SingleInstanceFileLock("PunterClient.lock");
    private Timer timer = new Timer(3000, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            setAppropriateTrayIcon();
        }

        private void setAppropriateTrayIcon() {
            BufferedImage requiredImage;
            if (isBusy()) {
                requiredImage = busyImage;

            } else if (!isConnected()) {
                requiredImage = dsctImage;
            } else {
                requiredImage = idleImage;
            }
            if (currentImage != requiredImage) {
                currentImage = requiredImage;
                trayIcon.setImage(currentImage);
                PunterGuiFrame.setIconImage(currentImage);
                KBFrame.setIconImage(currentImage);
            }
        }
    });
    private PunterGUI punterGUI;

    public Main() {

    }

    public void init() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    createAndShowGUI();
                } catch (Throwable e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        });
    }

    private void createAndShowGUI() throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        /*try {
            globalHotKeyListener = new GlobalHotKeyListener();
        } catch (Throwable t) {
            t.printStackTrace();
        }*/
        getAndSetUsername();
        KBFrame = new JFrame("Search");
        JFrame.setDefaultLookAndFeelDecorated(true);
        KBFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        KBFrame.setContentPane(new PunterKB(staticDaoFacade));
        if (AppSettings.getInstance().KBFrameLocation != null)
            KBFrame.setLocation(AppSettings.getInstance().KBFrameLocation);
        if (AppSettings.getInstance().getKBFrameDimension() != null)
            KBFrame.setPreferredSize(AppSettings.getInstance().getKBFrameDimension());
        KBFrame.pack();
        KBFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowIconified(WindowEvent e) {
                KBFrame.setVisible(false);
            }

            public void windowClosing(WindowEvent e) {
                KBFrame.setVisible(false);
//			displayMsg("Personal Assistant has been minimized to System Tray",TrayIcon.MessageType.INFO);
            }
        });

        PunterGuiFrame = new JFrame("My Personal Assistant");
        PunterGuiFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        punterGUI = new PunterGUI(staticDaoFacade, punterService);
        punterGUI.setGlobalHotKeyListener(globalHotKeyListener);
        PunterGuiFrame.setContentPane(punterGUI);

        PunterGuiFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowIconified(WindowEvent e) {
//	        			frame.dispose(); 
            }

            public void windowClosing(WindowEvent e) {
                //setVisible(false);
                PunterGuiFrame.dispose();
//	        			displayMsg("Assistant has been minimized to System Tray",TrayIcon.MessageType.INFO);
            }
        });
        PunterGuiFrame.pack();
        if (AppSettings.getInstance().PunterGuiFrameLocation != null)
            PunterGuiFrame.setLocation(AppSettings.getInstance().PunterGuiFrameLocation);
        Thread.UncaughtExceptionHandler handler = new StackWindow("Unhandled Exception", 500, 400, staticDaoFacade.getDevEmailCSV());
        Thread.setDefaultUncaughtExceptionHandler(handler);
       
      /* 
       System.setOut( new PrintStream(
				new ConsoleOutputStream (new Document(), System.out), true));
		System.setErr( new PrintStream(
				new ConsoleOutputStream (wcw.getLogArea().getDocument (), null), true));*/
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
            } catch (Exception e) {
                e.printStackTrace();
            }
            MouseListener mouseListener = new MouseListener() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (lastAccessed == null) {
                            lastAccessed = KBFrame;
                        }
                        lastAccessed.setVisible(true);
                        lastAccessed.setExtendedState(Frame.NORMAL);
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
                    globalHotKeyListener.cleanup();
                    timer.stop();
                    logger.log(Level.INFO, "Exiting...");
                }
            });

            ActionListener exitListener = e -> {
                int option = JOptionPane.showConfirmDialog(PunterGuiFrame, "Exit Punter?", "Confirm Exit", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    AppSettings.getInstance().KBFrameLocation = KBFrame.getLocation();
                    AppSettings.getInstance().setKBFrameDimension(KBFrame.getSize());
                    AppSettings.getInstance().PunterGuiFrameLocation = PunterGuiFrame.getLocation();
                    logger.log(Level.INFO, "Removing tray icon : " + KBFrame.getSize());
                    tray.remove(trayIcon);
                    Launcher.programQuit();
                }
            };

            PopupMenu popup = new PopupMenu();
            MenuItem openPunterMenuItem = new MenuItem("My Assistant");
            openPunterMenuItem.setFont(new Font("Tahoma", Font.BOLD, 12));
            openPunterMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    PunterGuiFrame.setVisible(true);
                    PunterGuiFrame.setExtendedState(Frame.NORMAL);
                    lastAccessed = PunterGuiFrame;
                }
            });
            popup.add(openPunterMenuItem);

            openPunterMenuItem = new MenuItem("Search");
            openPunterMenuItem.setFont(new Font("Tahoma", Font.BOLD, 12));
            openPunterMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    KBFrame.setVisible(true);
                    KBFrame.setExtendedState(Frame.NORMAL);
                    lastAccessed = KBFrame;
                }
            });
            popup.add(openPunterMenuItem);

            final MenuItem schedulerMenuItem = new MenuItem(AppSettings.getInstance().isSchedulerEnabled() ? "Stop Scheduler" : "Start Scheduler");
            schedulerMenuItem.setFont(new Font("Tahoma", Font.PLAIN, 12));
            schedulerMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //To change body of implemented methods use File | Settings | File Templates.
                    if (AppSettings.getInstance().isSchedulerEnabled()) {
                        punterGUI.stopPunterJobScheduler();
                        AppSettings.getInstance().setSchedulerEnabled(false);
                        schedulerMenuItem.setLabel("Start Scheduler");
                        logger.log(Level.INFO, "PunterJobScheduler Stopped");
                    } else {
                        punterGUI.startPunterJobScheduler();
                        AppSettings.getInstance().setSchedulerEnabled(true);
                        schedulerMenuItem.setLabel("Stop Scheduler");
                        logger.log(Level.INFO, "PunterJobScheduler Started");
                    }
                }
            });
            popup.add(schedulerMenuItem);

            final MenuItem clipboardMenuItem = new MenuItem("Stop Clipboard");
            clipboardMenuItem.setFont(new Font("Tahoma", Font.PLAIN, 12));
            clipboardMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (punterGUI.isClipboardListenerRunning()) {
                        punterGUI.stopClipBoardListener();
                        clipboardMenuItem.setLabel("Start Clipboard");
                        logger.log(Level.INFO, "Clipboard Stopped");
                    } else {
                        punterGUI.startClipBoardListener();
                        clipboardMenuItem.setLabel("Stop Clipboard");
                        logger.log(Level.INFO, "Clipboard Started");
                    }
                }
            });
            popup.add(clipboardMenuItem);

            MenuItem screenShotItem = new MenuItem("Capture Screen");
            screenShotItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JavaScreenCapture.captureScreenShot();
                }
            });
            popup.add(screenShotItem);

            MenuItem remoteSync = new MenuItem("Sync Remote");
            remoteSync.addActionListener(e -> {
                String remoteAddress = JOptionPane.showInputDialog("Enter remote base Url");
                if(remoteAddress!=null && !remoteAddress.isEmpty()){
                    punterService.syncRemoteDocuments(remoteAddress);
                }
            });
            popup.add(remoteSync);

            popup.addSeparator();

            MenuItem defaultItem = new MenuItem("Exit");
            defaultItem.addActionListener(exitListener);
            popup.add(defaultItem);

            trayIcon = new TrayIcon(idleImage, "My Assistant", popup);
            trayIcon.setToolTip("My Assistant started.");
            trayIcon.setImageAutoSize(true);

            ActionListener actionListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (lastAccessed == null) {
                        lastAccessed = KBFrame;
                    }
                    lastAccessed.setVisible(true);
                    lastAccessed.setExtendedState(Frame.NORMAL);
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
        timer.start();
    }

    private void getAndSetUsername() {
        String username = AppSettings.getInstance().getUsername();
        if (username == null || username.isEmpty()) {
            username = JOptionPane.showInputDialog("Enter NT Logon ID", System.getProperty("user.name"));
        }
        AppSettings.getInstance().setUsername(username);
    }

    public static boolean isBusy() {
        return ProcessExecutor.getInstance().isActive();
    }

    public boolean isConnected() {
        try {
            staticDaoFacade.ping();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.WARNING, "connection to server lost.");
            try {
                staticDaoFacade.makeConnection();
                logger.log(Level.WARNING, "connection to server restored.");
            } catch (Exception ee) {
                // TODO: handle exception
            }
            return false;
        }
    }

    public static void displayMsg(String msg, TrayIcon.MessageType msgType) {
        if (trayIcon != null) {
            trayIcon.displayMessage("My Punter", msg, msgType);
        }
    }
}
