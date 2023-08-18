package com.jiquan.rpc.loadbalance;

import java.net.InetSocketAddress;
import java.util.List;

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

	/**
	 * reload all the nodes
	 * @param serviceName
	 * @param addresses
	 */
	void reload(String serviceName, List<InetSocketAddress> addresses);
}
