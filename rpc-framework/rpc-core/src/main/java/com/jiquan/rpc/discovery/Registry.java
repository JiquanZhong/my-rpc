package com.jiquan.rpc.discovery;

import com.jiquan.rpc.ServiceConfig;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author ZHONG Jiquan
 * @year 2023
 * This class define two method for service provider and service consumer
 * @register allow the service provider register the service implementation info on register center
 * @lookup allows the service consumer to find the service info on register center
 */

public interface Registry {
	/**
	 * register the service on registry center
	 * @param serviceConfig the service configuration
	 */
	void register(ServiceConfig<?> serviceConfig);

	/**
	 * look up the available service on register center
	 * @param serviceName the service name that the consumer want to find on registry center
	 * @return
	 */
	List<InetSocketAddress> lookup(String serviceName);
}
