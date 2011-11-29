package com.sapient.punter.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

class ServiceSearcher{
	private Socket socket;
	private boolean alreadyRunning;

	public ServiceSearcher(final int socketPort){
		try {
			this.socket = new Socket(InetAddress.getLocalHost(), socketPort);
			alreadyRunning=true;
		} catch (IOException e) {
			alreadyRunning=false;
			e.printStackTrace();
		}
	}
	public boolean isAlreadyRunning() {
		return alreadyRunning;
	}
	
	public void kill() {
		try {
			OutputStream oStream = socket.getOutputStream();
			byte[] bytes = "EXIT".getBytes();
			oStream.write(bytes.length);
			oStream.write(bytes);
			oStream.flush();
			oStream.close();
		} catch (IOException e) {
			System.out.println("Couldn't talk to service");
		}finally{
			try {
				TimeUnit.SECONDS.sleep(2);
			} catch (InterruptedException e) {
			}
		}
	}
	public void disconnect(){
		try {
			OutputStream oStream = socket.getOutputStream();
			byte[] bytes = "NULL".getBytes();
			oStream.write(bytes.length);
			oStream.write(bytes);
			oStream.flush();
			oStream.close();
		} catch (IOException e) {
			System.out.println("Couldn't talk to service");
		}
	}
}