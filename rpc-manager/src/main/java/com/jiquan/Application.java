package com.jiquan;

import com.jiquan.utils.ZookeeperNode;
import com.jiquan.utils.ZookeeperUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
public class Application {
	public static final String CONNECTION = "10.188.78.86:2181,10.188.78.86:2182,10.188.78.86:2183";
	public static final int TIME_OUT = 10000;

	// Initialize the zookeeper nodes
	// yrpc-metadata (persistence)
	//  └─ providers (persistence)
	//  		└─ service1  (persistence, signature of method)
	//  		    ├─ node1 [data]     /ip:port
	//  		    ├─ node2 [data]
	//            └─ node3 [data]
	//  └─ consumers
	//        └─ service1
	//             ├─ node1 [data]
	//             ├─ node2 [data]
	//             └─ node3 [data]
	//  └─ config

	public static void main(String[] args) {
		ZooKeeper zookeeper = ZookeeperUtils.createZookeeper();

		//configuring the nodes and data
		String basePath = "/rpc-metadata";
		String providerPath = basePath + "/providers";
		String consumerPath = basePath + "/consumers";
		ZookeeperNode baseNode = new ZookeeperNode(basePath, null);
		ZookeeperNode providerNode = new ZookeeperNode(providerPath, null);
		ZookeeperNode consumerNode = new ZookeeperNode(consumerPath, null);
		// creating nodes
		List.of(baseNode, providerNode, consumerNode).forEach(node -> {
			ZookeeperUtils.createNode(zookeeper, node, null, CreateMode.PERSISTENT);
		});

		// closing zk connection
		ZookeeperUtils.close(zookeeper);
	}
}
