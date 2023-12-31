package com.jiquan.utils;

import com.jiquan.exceptions.NetworkException;
import lombok.extern.slf4j.Slf4j;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class NetUtils {
	public static String getIp(){
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()){
				NetworkInterface iface = interfaces.nextElement();
				if(iface.isLoopback() || iface.isVirtual() || !iface.isUp()){
					continue;
				}
				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while(addresses.hasMoreElements()){
					InetAddress addr = addresses.nextElement();
					if(addr instanceof Inet6Address || addr.isLoopbackAddress()){
						continue;
					}
					String ipAddress = addr.getHostAddress();
					if(log.isDebugEnabled()) log.info("the LAN ip address is: {}", ipAddress);
					return ipAddress;
				}
			}
			throw new NetworkException();
		} catch(SocketException e) {
			log.error("Error when getting the LAN info", e);
			throw new NetworkException(e);
		}
	}

	public static void main(String[] args) {
		String ip = getIp();
		System.out.println("ip = " + ip);
	}
}
