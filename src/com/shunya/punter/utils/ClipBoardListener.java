package com.shunya.punter.utils;

import com.shunya.kb.jpa.StaticDaoFacadeRemote;
import com.shunya.server.ClipboardPunterMessage;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;

public class ClipBoardListener implements ClipboardOwner, PunterComponent {
    Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
    private boolean listening = true;
    private boolean started = false;

    public ClipBoardListener() {
        startComponent();
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
                StaticDaoFacadeRemote.getInstance().sendMessageToPeer(punterMessage);
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
        sysClip.setContents(transferable, listening ? this : null);
    }

    public static void main(String[] args) {
        ClipBoardListener b = new ClipBoardListener();
        b.startComponent();
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