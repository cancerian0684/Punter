package com.sapient.punter.utils;

import java.awt.EventQueue;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;

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
				// Bring window to foreground
				setVisible(true);
				toFront();
				// Convert stack dump to string
				final StringWriter sw = new StringWriter();
				PrintWriter out = new PrintWriter(sw);
				t.printStackTrace(out);
				t.printStackTrace();
				// Add string to end of text area
				textArea.setText(sw.toString());

				final String localhost = "";
				final String mailhost = "namail.corp.adobe.com";
				final String mailuser = "munishc@adobe.com";
				final String email_notify = "munishc@adobe.com";
				new Thread() {
					@Override
					public void run() {
						MailNotifier mn = new MailNotifier(localhost, mailhost, mailuser, email_notify);
						try {
							InetAddress host = InetAddress.getLocalHost();
							mn.send("PDFG-Sync-Client-Crash :" + System.getProperty("user.name") + "/" + host.getHostName()
									+ "/" + System.getProperty("os.name"), sw.toString());
						} catch (Throwable E) {
							System.err.println(E.toString());
						}
					}
				}.start();

			}
		});

	}
}
