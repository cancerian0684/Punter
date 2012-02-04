package com.shunya.punter.utils;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.IntellitypeListener;
import com.melloware.jintellitype.JIntellitype;
import com.shunya.punter.gui.AppSettings;
import com.shunya.punter.gui.PunterGUI;
import com.shunya.punter.gui.PunterJobBasket;
import com.shunya.server.PunterProcessRunMessage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class GlobalHotKeyListener implements IntellitypeListener, HotkeyListener {
	private int taskIdToRun;
	private static final int WINDOWS_A = 88;
	private static final int WINDOWS_Q = 89;

	@Override
	public void onHotKey(int identifier) {
		output("WM_HOTKEY message received " + Integer.toString(identifier));
		Properties properties = new Properties();
		String props = (String) AppSettings.getInstance().getObject("appProperties");
		try {
            int id = 0;
			properties.load(new ByteArrayInputStream(props.getBytes()));
            switch (identifier){
                case WINDOWS_A:
                    id = Integer.parseInt(properties.getProperty(PunterGUI.WIN_A, "0"));
                    break;
                case WINDOWS_Q:
                    id = Integer.parseInt(properties.getProperty(PunterGUI.WIN_Q, "0"));
                    break;
            }
            PunterProcessRunMessage message=new PunterProcessRunMessage();
            message.setProcessId(id);
			PunterJobBasket.getInstance().addJobToBasket(message);
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
		JIntellitype.getInstance().registerHotKey(WINDOWS_Q, JIntellitype.MOD_WIN, 'Q');
	}

	private void unRegisterKeys() {
		JIntellitype.getInstance().unregisterHotKey(WINDOWS_A);
		JIntellitype.getInstance().unregisterHotKey(WINDOWS_Q);
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
}
