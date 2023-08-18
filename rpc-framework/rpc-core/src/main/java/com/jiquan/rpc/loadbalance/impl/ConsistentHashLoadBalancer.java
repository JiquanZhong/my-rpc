package com.jiquan.rpc.loadbalance.impl;

import com.jiquan.rpc.RpcBootstrap;
import com.jiquan.rpc.loadbalance.AbstractLoadBalancer;
import com.jiquan.rpc.loadbalance.Selector;
import com.jiquan.rpc.transport.message.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class ConsistentHashLoadBalancer extends AbstractLoadBalancer {
	@Override
	protected Selector getSelector(List<InetSocketAddress> serviceList) {
		return new ConsistentHashSelector(serviceList, 128);
	}

	public static class ConsistentHashSelector implements Selector{
		private SortedMap<Integer, InetSocketAddress> circle = new TreeMap<>();
		private int virtualNodesPerAddress;

		public ConsistentHashSelector(List<InetSocketAddress> serviceList, int virtualNodesPerAddress) {
			this.virtualNodesPerAddress = virtualNodesPerAddress;
			for(InetSocketAddress address : serviceList){
				addNodeToCircle(address);
			}
		}

		@Override
		public InetSocketAddress getNext() {
			RpcRequest rpcRequest = RpcBootstrap.REQUEST_THREAD_LOCAL.get();
			int hash = hash(Long.toString(rpcRequest.getRequestId()));
			// move key to the nearest clockwise position in the circle
			if(!circle.containsKey(hash)){
				SortedMap<Integer, InetSocketAddress> tailMap = circle.tailMap(hash);
				hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
			}
			return circle.get(hash);
		}

		private void addNodeToCircle(InetSocketAddress address){
			for(int i = 0; i < virtualNodesPerAddress; i++){
				int hash = hash(address.toString() + "-" + i);
				circle.put(hash, address);
				if(log.isDebugEnabled()) log.debug("virtual node {} is added to the circle", hash);
			}
		}

		private void removeNodeToCircle(InetSocketAddress address){
			for(int i = 0; i < virtualNodesPerAddress; i++){
				int hash = hash(address.toString() + "-" + i);
				circle.remove(hash);
				if(log.isDebugEnabled()) log.debug("virtual node {} is removed from the circle", hash);
			}
		}

		private int hash(String s){
			MessageDigest md;
			try {
				md = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
			byte[] digest = md.digest(s.getBytes());

			int res = 0;
			for (int i = 0; i < 4; i++) {
				res = res << 8;
				if( digest[i] < 0 ){
					res = res | (digest[i] & 255);
				} else {
					res = res | digest[i];
				}
			}
			return res;
		}
	}
}
