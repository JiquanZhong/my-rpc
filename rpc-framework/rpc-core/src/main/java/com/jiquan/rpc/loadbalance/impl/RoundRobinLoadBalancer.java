package com.jiquan.rpc.loadbalance.impl;

import com.jiquan.exceptions.LoadBalanceException;
import com.jiquan.rpc.loadbalance.AbstractLoadBalancer;
import com.jiquan.rpc.loadbalance.Selector;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class RoundRobinLoadBalancer extends AbstractLoadBalancer {
	@Override
	protected Selector getSelector(List<InetSocketAddress> serviceList) {
		return new RoundRobinSelector(serviceList);
	}

	private static class RoundRobinSelector implements Selector{
		private List<InetSocketAddress> serviceList;
		private AtomicInteger index = new AtomicInteger(0);

		public RoundRobinSelector(List<InetSocketAddress> serviceList) {
			this.serviceList = serviceList;
		}

		@Override
		public InetSocketAddress getNext() {
			if(serviceList == null || serviceList.size() == 0){
				log.error("the service nodes is empty");
				throw new LoadBalanceException();
			}

			InetSocketAddress address = serviceList.get(index.get());

			if(index.get() == serviceList.size() - 1){
				index.set(0);
			}else {
				index.incrementAndGet();
			}
			return address;
		}

		@Override
		public void reBalance() {

		}
	}
}
