package com.sapient.punter.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyThread extends Thread {
	private PipedInputStream in;
	private BufferedReader bufOut;
	private StringBuilder sb = new StringBuilder();
	private volatile boolean found = false;
	private Logger logger;
	public MyThread(PipedInputStream in,Logger logger) {
		this.in = in;
		this.bufOut = new BufferedReader(new InputStreamReader(in));
		this.logger=logger;
	}

	public synchronized String getResult() {
		try {
			while (!found)
				wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		sb.append("----------------xxxxxxxxxxxx---------------" + "\r\n");
		String result = sb.toString();
		// System.out.println(result);
		sb.setLength(0);
		found = false;
		notifyAll();
		return result;
	}

	public synchronized void run() {
		String line = null;
		try {
			while ((line = bufOut.readLine()) != null) {
//				 System.err.println(line);
//				 logger.log(Level.INFO, line);
				sb.append(line + "\r\n");
				if (line.equalsIgnoreCase("munish1234")) {
					sb.append("..." + "\r\n");
//					System.err.println("----------------xxxxxx-----------------");
					found = true;
					notifyAll();
					try {
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	};
}