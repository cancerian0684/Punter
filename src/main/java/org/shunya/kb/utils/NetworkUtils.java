package org.shunya.kb.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class NetworkUtils {
    public static void main(String[] args) {
        System.out.println(GetMacAddress());
    }

    private static String GetMacAddress() {
        try {
            InetAddress address = InetAddress.getLocalHost();
//          InetAddress address = InetAddress.getByName("192.168.46.53");
            NetworkInterface ni = NetworkInterface.getByInetAddress(address);
            if (ni != null) {
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    PrintStream ps = new PrintStream(byteArrayOutputStream);
                    for (int i = 0; i < mac.length; i++) {
                        ps.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : "");
                    }
                    return byteArrayOutputStream.toString();
                } else {
                    System.out.println("Address doesn't exist or is not accessible.");
                }
            } else {
                System.out.println("Network Interface for the specified address is not found.");
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }
}
