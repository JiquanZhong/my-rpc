package com.jiquan.rpc.watcher;

import com.jiquan.rpc.NettyBootstrapInitializer;
import com.jiquan.rpc.RpcBootstrap;
import com.jiquan.rpc.discovery.Registry;
import com.jiquan.rpc.loadbalance.LoadBalancer;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class UpAndDownWatcher implements Watcher {
	@Override
	public void process(WatchedEvent event) {
		if (event.getType() == Event.EventType.NodeChildrenChanged){
			if (log.isDebugEnabled()){
				log.debug("Detecting the change of node {}",event.getPath());
			}
			try {
				Thread.sleep(10000);
			}catch(InterruptedException e){

			}
			String serviceName = getServiceName(event.getPath());
			Registry registry = RpcBootstrap.getInstance().getRegistry();
			List<InetSocketAddress> addresses = registry.lookup(serviceName);
			// two cases: node removed and node added
			for(InetSocketAddress address : addresses){
				// case 1: node added
				if(!RpcBootstrap.CHANNEL_CACHE.containsKey(address)){
					Channel channel;
					try {
						channel = NettyBootstrapInitializer
								.getBootstrap()
								.connect(address)
								.sync().channel();
					} catch(InterruptedException e) {
						throw new RuntimeException(e);
					}
					RpcBootstrap.CHANNEL_CACHE.put(address, channel);
				}
			}

			// case 2: node removed
			for(Map.Entry<InetSocketAddress, Channel> entry : RpcBootstrap.CHANNEL_CACHE.entrySet()){
				if(!addresses.contains(entry.getKey())){
					RpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
				}
			}

			LoadBalancer loadBalance = RpcBootstrap.LOAD_BALANCE;
			loadBalance.reload(serviceName, addresses);
		}
	}

	private String getServiceName(String path){
		String[] split = path.split("/");
		return split[split.length-1];
	}
}
