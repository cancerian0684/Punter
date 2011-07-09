package com.sapient.punter.utils;

import java.lang.reflect.Method;

import javax.swing.JOptionPane;
public class Launcher {
	static final int socketPort = 9876;
	private ServiceSearcher existingService;
	
	public void launch(String className) {
		System.out.println("Trying to launch:" + className);
		existingService=new ServiceSearcher(socketPort);
		if (existingService.isAlreadyRunning()) {
			Object[] options = { "Replace Instance", "Cancel Launch" };
			int userChoice = JOptionPane.showOptionDialog(null, "An instance of this program already running. Want to ?", "Instance Found !",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			System.out.println("found running service");
			if (userChoice == JOptionPane.YES_OPTION) {
				existingService.kill();
			} else {
				existingService.disconnect();
				System.exit(0);
			}
		} 
		startNewInstance(className);
	}

	private void startNewInstance(String className) {
		System.out.println("Starting new service");
		Launcher.invokeMethod(className);
		Thread listener = new ListenerThread();
		listener.start();
		System.out.println("started service listener");
	}

	public static synchronized void invokeMethod(final String className) {
		System.out.println("running a " + className);
		Thread thread = new Thread() {
			public void run() {
				try {
					Class clazz = Class.forName(className);
					Class[] argsTypes = { String[].class };
					Object[] args = { new String[0] };
					Method method = clazz.getMethod("main", argsTypes);
					method.invoke(clazz, args);
				} catch (Exception e) {
					System.out.println("coudn't run the :" + className);
					e.printStackTrace();
					System.out.println(e.getMessage());
				}
			}
		};
		thread.start();
		runningPrograms++;
	}

	static int runningPrograms = 0;

	public static void programQuit() {
		runningPrograms--;
		if (runningPrograms <= 0) {
			System.exit(0);
		}
	}

	public static void main(String[] args) {
		Launcher l = new Launcher();
		l.launch("com.sapient.punter.gui.Main");
	}
}
