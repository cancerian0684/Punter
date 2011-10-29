package com.sapient.punter.utils;

import com.sapient.kb.jpa.StaticDaoFacade;
import com.sapient.server.ClipboardPunterMessage;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;

public class ClipBoardListener extends Thread implements ClipboardOwner {
    Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();

    public void run() {
        Transferable trans = sysClip.getContents(this);
        regainOwnership(trans);
    }

    public void lostOwnership(Clipboard clipboard, Transferable transferable) {
        try {
            // sleep this thread so that other application is done with the Clipboard access.
            // otherwise an exception is thrown by the application.
            this.sleep(50);
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
        Transferable contents = sysClip.getContents(this);
        processContents(contents);
        regainOwnership(contents);
    }

    public void handleContent(ClipboardPunterMessage punterMessage) {
        String contents = punterMessage.getContents();
        System.out.println(contents);
        StringSelection ss = new StringSelection(contents);
        sysClip.setContents(ss, this);
    }

    void processContents(Transferable transferable) {
        if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                String s = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                System.out.println(s);
                ClipboardPunterMessage punterMessage = new ClipboardPunterMessage();
                punterMessage.setContents(s);
                StaticDaoFacade.getInstance().sendMessageToPeer(punterMessage);
            } catch (UnsupportedFlavorException e2) {
                e2.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void regainOwnership(Transferable transferable) {
        sysClip.setContents(transferable, this);
    }

    public static void main(String[] args) {
        ClipBoardListener b = new ClipBoardListener();
        b.start();
    }
}