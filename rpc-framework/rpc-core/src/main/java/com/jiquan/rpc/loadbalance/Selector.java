package com.jiquan.rpc.loadbalance;

import java.net.InetSocketAddress;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
public interface Selector {

	/**
	 * get the next host:port of a service
	 * @return
	 */
	InetSocketAddress getNext();

	/**
	 * need to recalculate the load because the nodes of service is changed
	 */
	void reBalance();
}
