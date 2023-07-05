package com.jiquan.rpc.loadbalance;

import com.jiquan.rpc.RpcBootstrap;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
public abstract class AbstractLoadBalancer implements LoadBalancer{

	private Map<String, Selector> cache = new ConcurrentHashMap<>(8);

	@Override
	public InetSocketAddress selectServiceAddress(String serviceName) {
		Selector selector = cache.get(serviceName);
		if(selector == null){
			List<InetSocketAddress> addresses = RpcBootstrap.getInstance().getRegistry().lookup(serviceName);
			selector = getSelector(addresses);
			cache.put(serviceName, selector);
		}
		return selector.getNext();
	}

	protected abstract Selector getSelector(List<InetSocketAddress> serviceList);
}
