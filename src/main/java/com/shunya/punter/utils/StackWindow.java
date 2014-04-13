package com.shunya.punter.utils;

import com.shunya.punter.gui.AppSettings;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

public class StackWindow extends JFrame implements Thread.UncaughtExceptionHandler {

    private final String devEmailCSV;
    private JTextArea textArea;

	public static void main(String[] args) {
		Thread.UncaughtExceptionHandler handler = new StackWindow("Unhandled Exception", 500, 400, "munishc@xxx.com");
		Thread.setDefaultUncaughtExceptionHandler(handler);
		throw new RuntimeException("should be caught");
	}

	public StackWindow(String title, final int width, final int height, String devEmailCSV) {
		super(title);
        this.devEmailCSV = devEmailCSV;
        setSize(width, height);
		textArea = new JTextArea();
		JScrollPane pane = new JScrollPane(textArea);
		textArea.setEditable(false);
		getContentPane().add(pane);
		setLocationRelativeTo(null);
	}

	public void uncaughtException(Thread t, Throwable e) {
		addStackInfo(e);
	}

	public void addStackInfo(final Throwable t) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				final StringWriter sw = new StringWriter();
				PrintWriter out = new PrintWriter(sw);
				t.printStackTrace(out);
				t.printStackTrace();
				new Thread() {
					@Override
					public void run() {
						try {
                            EmailService.getInstance().sendEMail("Unknown Exception : [" + AppSettings.getInstance().getUsername() + "] ", devEmailCSV, sw.toString());
                            Thread.sleep(500);
                            System.exit(0);
						} catch (Throwable E) {
							System.err.println(E.toString());
						}
					}
				}.start();
                setVisible(true);
				toFront();
                textArea.setText(sw.toString());
			}
		});

	}
}
