package com.jiquan.rpc.loadbalance.impl;

import com.jiquan.rpc.RpcBootstrap;
import com.jiquan.rpc.loadbalance.AbstractLoadBalancer;
import com.jiquan.rpc.loadbalance.Selector;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
public class MinimumResponseTimeLoadBalancer extends AbstractLoadBalancer {
	@Override
	protected Selector getSelector(List<InetSocketAddress> serviceList) {
		return new MinimumResponseTimeSelector(serviceList);
	}

	private static class MinimumResponseTimeSelector implements Selector{
		public MinimumResponseTimeSelector(List<InetSocketAddress> serviceList) {

		}

		@Override
		public InetSocketAddress getNext() {
			Map.Entry<Long, Channel> entry = RpcBootstrap.ANSWER_TIME_CACHE.firstEntry();
			if(entry != null) return (InetSocketAddress) entry.getValue().remoteAddress();
			Channel channel = (Channel) RpcBootstrap.CHANNEL_CACHE.values().toArray()[0];
			return (InetSocketAddress) channel.remoteAddress();
		}
	}
}
