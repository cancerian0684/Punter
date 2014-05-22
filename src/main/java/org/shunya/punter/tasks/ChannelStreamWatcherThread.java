package org.shunya.punter.tasks;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.Reader;
import java.util.logging.Logger;

public class ChannelStreamWatcherThread extends Thread {
	private PipedInputStream channelStream;
	private String token="\nmunish1234";
	private StringBuffer sb = new StringBuffer();
	private volatile boolean found = false;
	public void resetToken(){
		  token="\nmunish1234";
	}
	public void setToken(String token) {
		this.token = token;
	}
	public ChannelStreamWatcherThread(PipedInputStream channelStream, Logger logger) {
		this.channelStream = channelStream;
	}

	public synchronized String waitForToken(long timeout) {
		try {
			while (!found)
				wait(timeout);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		sb.append("----------------xxxxxxxxxxxx---------------" + "\r\n");
		String result = sb.toString();
		sb.delete(0, sb.length());
		sb.setLength(0);
		found = false;
		notifyAll();
		return result;
	}

	public synchronized void run() {
		try {
			  Reader reader = new InputStreamReader(this.channelStream);
			  int data = reader.read();
			  while(data != -1){
			      char theChar = (char) data;
			      sb.append(theChar);
			      if(sb.toString().contains(token)){
			    	      resetToken();
						  sb.append(" -- Found token.."+"\r\n");
//						  System.err.println(sb.toString());
						  found=true;
						  notifyAll();
						  try {
							wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
			      }
			      data = reader.read();
			  }
			  reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	  };
}