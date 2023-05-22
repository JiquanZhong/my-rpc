package com.jiquan.utils;

import com.jiquan.Constant;
import com.jiquan.exceptions.ZookeeperException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class ZookeeperUtils {

	public static ZooKeeper createZookeeper() {
		String connectString = Constant.DEFAULT_ZK_CONNECTION;
		int timeout = Constant.TIME_OUT;
		return createZookeeper(connectString, timeout);
	}

	/**
	 * create a zookeeper connection
	 *
	 * @param connectString connection ip:port
	 * @param timeout       timeout of connection
	 * @return the instance of zk connection
	 */
	public static ZooKeeper createZookeeper(String connectString, int timeout) {
		CountDownLatch countDownLatch = new CountDownLatch(1);
		try {
			final ZooKeeper zooKeeper = new ZooKeeper(connectString, timeout, new Watcher() {
				@Override
				public void process(WatchedEvent watchedEvent) {
					if(watchedEvent.getState() == Event.KeeperState.SyncConnected) {
						log.debug("successful zookeeper connection");
						countDownLatch.countDown();
					}
				}
			});
			countDownLatch.await();
			return zooKeeper;
		} catch(IOException | InterruptedException e) {
			log.error("exception when creating zk node", e);
			throw new ZookeeperException();
		}
	}

	/**
	 * creating a node with the zk connection
	 * @param zooKeeper zk connection instance
	 * @param node new node path
	 * @param watcher the watcher of new node
	 * @param createMode creating mode for new node
	 * @return true successful creation, false the node exists already
	 */
	public static Boolean createNode(ZooKeeper zooKeeper, ZookeeperNode node, Watcher watcher, CreateMode createMode) {
		try {
			if(zooKeeper.exists(node.getNodePath(), watcher) == null) {
				String result = zooKeeper.create(node.getNodePath(), node.getData(),
												 ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
				log.info("creation of node {} succeeds", result);
				return true;
			} else {
				if(log.isDebugEnabled()){
					log.info("the nodes exist already", node.getNodePath());
				}
				return false;
			}
		} catch(KeeperException | InterruptedException e) {
			log.error("error when creating the base nodes", e);
			throw new ZookeeperException();
		}
	}

	/**
	 * closing a zk connection
	 * @param zooKeeper the target zk connection
	 */
	public static void close(ZooKeeper zooKeeper){
		try {
			zooKeeper.close();
		}catch(InterruptedException e){
			log.error("error when closing the connection", e);
			throw new ZookeeperException();
		}
	}

	/**
	 * getting the children node with the specified zk connection and path
	 * @param zooKeeper zk connection
	 * @param serviceNode the target path of zk
	 * @param watcher the watcher applying on the path
	 * @return
	 */
	public static List<String> getChildren(ZooKeeper zooKeeper, String serviceNode, Watcher watcher){
		try {
			return zooKeeper.getChildren(serviceNode, watcher);
		}catch(KeeperException | InterruptedException e){
			log.error("error when getting the {}'s children node", serviceNode, e);
			throw new ZookeeperException();
		}
	}

	public static boolean exists(ZooKeeper zk, String node, Watcher watcher) {
		try {
			return zk.exists(node,watcher) != null;
		}catch(KeeperException | InterruptedException e){
			log.error("Error when verifying the existence of {}", node, e);
			throw new ZookeeperException(e);
		}
	}
}
