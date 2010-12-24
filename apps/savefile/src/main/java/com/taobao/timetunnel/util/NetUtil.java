package com.taobao.timetunnel.util;
import java.net.*;
public class NetUtil {
	public static void main(String args[]) {
		System.out.println(getLocalHostName());
	}
    public static String getLocalHostName() {
        try {
                return InetAddress.getLocalHost().getHostName();
        }
        catch (Exception e) {
                return "localhost";
        }
    }
}
