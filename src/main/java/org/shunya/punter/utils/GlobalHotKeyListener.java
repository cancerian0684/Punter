package org.shunya.punter.utils;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.IntellitypeListener;
import com.melloware.jintellitype.JIntellitype;
import org.shunya.punter.gui.*;
import org.shunya.punter.gui.Main;
import org.shunya.server.PunterProcessRunMessage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class GlobalHotKeyListener implements IntellitypeListener, HotkeyListener {
    private int taskIdToRun;
    private static final int WINDOWS_A = 88;
    private PunterWindow punterGui;

    @Override
    public void onHotKey(int identifier) {
        output("WM_HOTKEY message received " + Integer.toString(identifier));
        Properties properties = new Properties();
        String props = (String) AppSettings.getInstance().getObject("appProperties");
        try {
            int id = 0;
            properties.load(new ByteArrayInputStream(props.getBytes()));
            switch (identifier) {
                case WINDOWS_A:
                    punterGui.setGUIVisible();
                    break;
                case 1:
                    id = Integer.parseInt(properties.getProperty(PunterGUI.WIN_1, "0"));
                    break;
                case 2:
                    id = Integer.parseInt(properties.getProperty(PunterGUI.WIN_2, "0"));
                    break;
                case 3:
                    id = Integer.parseInt(properties.getProperty(PunterGUI.WIN_3, "0"));
                    break;
            }
            if (id != 0) {
                PunterProcessRunMessage message = new PunterProcessRunMessage();
                message.setProcessId(id);
                PunterJobBasket.getInstance().addJobToBasket(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onIntellitype(int command) {
        // TODO Auto-generated method stub

    }

    public void cleanup() {
        unRegisterKeys();
        JIntellitype.getInstance().cleanUp();
    }

    public GlobalHotKeyListener() {
        System.out.println(new File(".").getAbsolutePath());
        JIntellitype.setLibraryLocation("JIntellitype.dll");
        if (JIntellitype.checkInstanceAlreadyRunning("JIntellitype Test Application")) {
            throw new RuntimeException("Global Hot keys already registered.");
        }

        // next check to make sure JIntellitype DLL can be found and we are on
        // a Windows operating System
        if (!JIntellitype.isJIntellitypeSupported()) {
            throw new RuntimeException("either dll's or required os not found.");
        }
        initJIntellitype();
        registerKeys();
    }

    private void registerKeys() {
        JIntellitype.getInstance().registerHotKey(WINDOWS_A, JIntellitype.MOD_WIN, 'A');
        JIntellitype.getInstance().registerHotKey(1, JIntellitype.MOD_ALT + JIntellitype.MOD_CONTROL, '1');
        JIntellitype.getInstance().registerHotKey(2, JIntellitype.MOD_ALT + JIntellitype.MOD_CONTROL, '2');
        JIntellitype.getInstance().registerHotKey(3, JIntellitype.MOD_ALT + JIntellitype.MOD_CONTROL, '3');
        JIntellitype.getInstance().registerHotKey(4, JIntellitype.MOD_ALT + JIntellitype.MOD_CONTROL, '4');
        JIntellitype.getInstance().registerHotKey(5, JIntellitype.MOD_ALT + JIntellitype.MOD_CONTROL, '5');
        JIntellitype.getInstance().registerHotKey(6, JIntellitype.MOD_ALT + JIntellitype.MOD_CONTROL, '6');
        JIntellitype.getInstance().registerHotKey(7, JIntellitype.MOD_ALT + JIntellitype.MOD_CONTROL, '7');
        JIntellitype.getInstance().registerHotKey(8, JIntellitype.MOD_ALT + JIntellitype.MOD_CONTROL, '8');
        JIntellitype.getInstance().registerHotKey(9, JIntellitype.MOD_ALT + JIntellitype.MOD_CONTROL, '9');
    }

    private void unRegisterKeys() {
        JIntellitype.getInstance().unregisterHotKey(WINDOWS_A);
        JIntellitype.getInstance().unregisterHotKey(1);
        JIntellitype.getInstance().unregisterHotKey(2);
        JIntellitype.getInstance().unregisterHotKey(3);
        JIntellitype.getInstance().unregisterHotKey(4);
        JIntellitype.getInstance().unregisterHotKey(5);
        JIntellitype.getInstance().unregisterHotKey(6);
    }

    public void initJIntellitype() {
        try {
            JIntellitype.getInstance().addHotKeyListener(this);
            JIntellitype.getInstance().addIntellitypeListener(this);
            output("JIntellitype initialized");
        } catch (RuntimeException ex) {
            output("Either you are not on Windows, or there is a problem with the JIntellitype library!");
        }
    }

    private void output(String text) {
        System.out.println(text);
    }

    public int getTaskIdToRun() {
        return taskIdToRun;
    }

    public void setTaskIdToRun(int taskIdToRun) {
        this.taskIdToRun = taskIdToRun;
    }

    public void setPunterGui(Main punterGui) {
        this.punterGui = punterGui;
    }
}
