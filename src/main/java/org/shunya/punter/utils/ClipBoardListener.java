package org.shunya.punter.utils;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.shunya.server.ClipboardPunterMessage;
import org.shunya.server.component.DBService;
import org.shunya.server.component.PunterService;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ClipBoardListener implements ClipboardOwner, PunterComponent {
    private final DBService dbService;
    private final PunterService punterService;
    private final Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
    private boolean listening = true;
    private boolean started = false;

    public ClipBoardListener(DBService dbService, PunterService punterService) {
        this.dbService = dbService;
        this.punterService = punterService;
//        startComponent();
    }

    public void run() {
        Transferable transferable = sysClip.getContents(this);
        regainOwnership(transferable);
    }

    public void lostOwnership(Clipboard clipboard, Transferable transferable) {
        try {
            // sleep this thread so that other application is done with the Clipboard access.
            // otherwise an exception is thrown by the application.
            Thread.sleep(100);
            Transferable contents = sysClip.getContents(this);
            processContents(contents);
            regainOwnership(contents);
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }
    }

    public void handleContent(ClipboardPunterMessage punterMessage) {
        if(punterMessage.getType().equals("string")) {
            Object contents = punterMessage.getContents();
            System.out.println(contents);
            StringSelection ss = new StringSelection((String) contents);
            sysClip.setContents(ss, this);
        }else if(punterMessage.getType().equals("image")){
            try {
                byte[] bytearray = Base64.decode(punterMessage.getContents());
                BufferedImage imag = ImageIO.read(new ByteArrayInputStream(bytearray));
                ImageSelection ss = new ImageSelection(imag);
                sysClip.setContents(ss, this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void processContents(Transferable transferable) {
        if(listening) {
            if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                try {
                    String transferData = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                    System.out.println(transferData);
                    ClipboardPunterMessage punterMessage = new ClipboardPunterMessage();
                    punterMessage.setType("string");
                    punterMessage.setContents(transferData);
                    punterService.sendMessageToPeers(punterMessage);
                } catch (UnsupportedFlavorException | IOException | InterruptedException e2) {
                    e2.printStackTrace();
                }
            }else if(transferable.isDataFlavorSupported(DataFlavor.imageFlavor)){
                try {
                    BufferedImage img = (BufferedImage) transferable.getTransferData(DataFlavor.imageFlavor);
                    ByteArrayOutputStream baos=new ByteArrayOutputStream(1000);
                    ImageIO.write(img, "png", baos);
                    baos.flush();
                    String base64String= Base64.encode(baos.toByteArray());
                    baos.close();
                    ClipboardPunterMessage punterMessage = new ClipboardPunterMessage();
                    punterMessage.setType("image");
                    punterMessage.setContents(base64String);
                    punterService.sendMessageToPeers(punterMessage);
                } catch (UnsupportedFlavorException | IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void regainOwnership(Transferable transferable) {
        sysClip.setContents(transferable, listening ? this : null);
    }

    @Override
    public void startComponent() {
        if (!started) {
            listening = true;
            run();
            started = true;
        }
    }

    @Override
    public void stopComponent() {
        if (started) {
            listening = false;
            run();
            started = false;
        }
    }

    @Override
    public boolean isStarted() {
        return started;
    }
}