package com.shunya.punter.utils;

import com.shunya.kb.jpa.StaticDaoFacade;
import com.shunya.punter.gui.AppSettings;

import java.awt.EventQueue;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class StackWindow extends JFrame implements Thread.UncaughtExceptionHandler {

	private JTextArea textArea;

	public static void main(String[] args) {
		Thread.UncaughtExceptionHandler handler = new StackWindow("Unhandled Exception", 500, 400);
		Thread.setDefaultUncaughtExceptionHandler(handler);
		throw new RuntimeException("should be caught");
	}

	public StackWindow(String title, final int width, final int height) {
		super(title);
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
                            EmailService.getInstance().sendEMail("Unknown Exception : [" + AppSettings.getInstance().getUsername() + "] ", StaticDaoFacade.getInstance().getDevEmailCSV(), sw.toString());
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
