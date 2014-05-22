package org.shunya.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MultiCastResponder {
	private static MultiCastResponder mcr;
	private volatile boolean keepRunning=true;
	public static MultiCastResponder getInstance(){
		if(mcr==null){
			try {
				mcr=new MultiCastResponder();
				System.err.println("MultiClient Responder STARTED.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mcr;
	}
	private MultiCastResponder() throws Exception{
	    final MulticastSocket socket = new MulticastSocket(4446);
	    final InetAddress address = InetAddress.getByName("230.0.0.1");
		socket.joinGroup(address);
	    Thread thread=new Thread(){
			public void run() {		
				try {
					checkForPause();
					while(keepRunning) {
						checkForPause();
					    byte[] buf = new byte[256];
					    DatagramPacket packet;
			            packet = new DatagramPacket(buf, buf.length);
			            socket.receive(packet);
			            System.out.println(packet.getAddress().getHostAddress());
			            String received = new String(packet.getData(), 0, packet.getLength());
			            System.out.println("Connection received at : " + received +" from the Client : "+packet.getAddress().getHostAddress());
			            
			            InetAddress address1 = packet.getAddress();
			            DatagramSocket socket1 = new DatagramSocket();
			            String resBytes=address1.getHostAddress()+":1099";
			            DatagramPacket packet1 = new DatagramPacket(resBytes.getBytes(), resBytes.getBytes().length,address1, 4448);
			            socket1.send(packet1);
			            System.out.println("packet sent back to the client.");
					}
					socket.leaveGroup(address);
					socket.close();
					System.err.println("MultiClient Responder Shutdown COMPLETED ");
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		};
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.setDaemon(true);
		thread.start();
	}
	public void shutdown(){
		keepRunning=false;
	}
	public synchronized boolean toggle(){
		if(keepRunning)
			keepRunning=false;
		else
			keepRunning=true;
		notify();
		return keepRunning;
	}
	public synchronized void checkForPause(){
		while(keepRunning==false){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
