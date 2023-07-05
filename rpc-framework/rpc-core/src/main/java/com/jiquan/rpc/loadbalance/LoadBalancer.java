package com.jiquan.rpc.loadbalance;

import java.net.InetSocketAddress;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
public interface LoadBalancer {
	/**
	 * get the next service address applying the load balance strategy
	 * @param serviceName
	 * @return
	 */
	InetSocketAddress selectServiceAddress(String serviceName);
}
