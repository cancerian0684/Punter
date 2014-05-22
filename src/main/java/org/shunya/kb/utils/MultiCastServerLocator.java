package org.shunya.kb.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Date;

public class MultiCastServerLocator {
	protected DatagramSocket socket = null;
	private DatagramSocket socket1 =null;
	protected int retry = 30;

	public MultiCastServerLocator() {
		try {
			socket = new DatagramSocket(4445);
			socket.setSoTimeout(5000);
			socket1 = new DatagramSocket(4448);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	public String LocateServerAddress() {
        while (retry>0) {
            try {
            	retry=retry-1;
                byte[] buf = new byte[256];
                // construct quote
                String dString = null;
                dString = new Date().toString();
                buf = dString.getBytes();
		        // send it
                InetAddress group = InetAddress.getByName("230.0.0.1");
                DatagramPacket packet = new DatagramPacket(buf, buf.length, group, 4446);
                socket.send(packet);
                
                // recieve the response
                socket1.setSoTimeout(3000);
                DatagramPacket packet1 = new DatagramPacket(buf, buf.length);

                socket1.receive(packet1);
                String received = new String(packet1.getData(), 0, packet1.getLength());
                System.out.println("Location of the Server: " + packet1.getAddress().getHostAddress()+" - "+received);
                socket1.close();
                socket.close();
                System.err.println("All sockets closed succesfully.");
                return packet1.getAddress().getHostAddress();
            	} catch (IOException e) {
                System.err.println("Did not receive any reply from server in 3 seconds."+e.getMessage());
            }
        }
    socket1.close();
	socket.close();
	System.err.println("All sockets closed succesfully.");
	return "";
    }
}
