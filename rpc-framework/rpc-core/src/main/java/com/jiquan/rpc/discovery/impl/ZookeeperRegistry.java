package com.jiquan.rpc.discovery.impl;

import com.jiquan.Constant;
import com.jiquan.exceptions.DiscoveryException;
import com.jiquan.rpc.RpcBootstrap;
import com.jiquan.rpc.ServiceConfig;
import com.jiquan.rpc.discovery.AbstractRegistry;
import com.jiquan.rpc.watcher.UpAndDownWatcher;
import com.jiquan.utils.NetUtils;
import com.jiquan.utils.ZookeeperNode;
import com.jiquan.utils.ZookeeperUtils;
import io.netty.util.NetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class ZookeeperRegistry extends AbstractRegistry {
	private ZooKeeper zookeeper;

	/**
	 * creating a zookeeper connection with the default settings
	 * see com.jiquan.utils.ZookeeperUtils
	 */
	public ZookeeperRegistry() {
		this.zookeeper = ZookeeperUtils.createZookeeper();
	}

	/**
	 * creating a zookeeper connection instance with the input settings
	 * see com.jiquan.utils.ZookeeperUtils
	 *
	 * @param connectString comma separated host:port pairs, each corresponding to a zk server.
	 *                      e.g. "127.0.0.1:3000,127.0.0.1:3001,127.0.0.1:3002" If the optional chroot suffix is used
	 *                      the example would look like: "127.0.0.1:3000,127.0.0.1:3001,127.0.0.1:3002/app/a" where
	 *                      the client would be rooted at "/app/a" and all paths would be relative to this root -
	 *                      ie getting/setting/etc... "/foo/bar" would result in operations being run on "/app/a/foo/bar"
	 *                      (from the server perspective).
	 * @param timeout       session timeout in milliseconds
	 */
	public ZookeeperRegistry(String connectString, int timeout) {
		this.zookeeper = ZookeeperUtils.createZookeeper(connectString, timeout);
	}

	// Create a temporary node of this machine, ip:port,
	// The port of the service provider is generally set by itself, and we also need a method to obtain the ip
	// ip We usually need a LAN ip, not 127.0.0.1, nor ipv6
	// 10.188.78.86
	//TODO: Subsequent processing port issues
	@Override
	public void register(ServiceConfig<?> serviceConfig) {
		// the name of service node
		String parentNode = Constant.BASE_PROVIDER_PATH + "/" + serviceConfig.getInterface().getName();
		if(!ZookeeperUtils.exists(zookeeper, parentNode, null)) {
			ZookeeperNode zookeeperNode = new ZookeeperNode(parentNode, null);
			ZookeeperUtils.createNode(zookeeper, zookeeperNode, null, CreateMode.PERSISTENT);
		}

		String node = parentNode + "/" + NetUtils.getIp() + ":" + RpcBootstrap.PORT;
		if(!ZookeeperUtils.exists(zookeeper, node, null)) {
			ZookeeperNode zookeeperNode = new ZookeeperNode(node, null);
			ZookeeperUtils.createNode(zookeeper, zookeeperNode, null, CreateMode.EPHEMERAL);
		}

		if(log.isDebugEnabled()) log.debug("the service {} is registered", serviceConfig.getInterface().getName());
	}

	@Override
	public List<InetSocketAddress> lookup(String serviceName) {
		String serviceNode = Constant.BASE_PROVIDER_PATH + "/" + serviceName;
		List<String> children = ZookeeperUtils.getChildren(zookeeper, serviceNode, new UpAndDownWatcher());
		List<InetSocketAddress> inetSocketAddresses = children.stream().map(ipString -> {
			String[] ipAndPort = ipString.split(":");
			String ip = ipAndPort[0];
			int port = Integer.parseInt(ipAndPort[1]);
			return new InetSocketAddress(ip, port);
		}).toList();

		if(inetSocketAddresses.size() == 0)
			throw new DiscoveryException("Cannot find the target host with: " + serviceNode);

		return inetSocketAddresses;
	}
}
