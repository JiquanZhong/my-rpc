package com.jiquan.rpc.discovery;

import com.jiquan.Constant;
import com.jiquan.exceptions.DiscoveryException;
import com.jiquan.rpc.discovery.impl.NacosRegistry;
import com.jiquan.rpc.discovery.impl.ZookeeperRegistry;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
public class RegistryConfig {
	private String connectString;


	public RegistryConfig(String connectString) {
		this.connectString = connectString;
	}

	public Registry getRegistry(){
		String registryType = getRegistryType(connectString, true);
		if(registryType.equals("zookeeper")){
			String host = getRegistryType(connectString, false);
			return new ZookeeperRegistry(host, Constant.TIME_OUT);
		}else if(registryType.equals("nacos")){
			String host = getRegistryType(connectString, false);
			return new NacosRegistry(host, Constant.TIME_OUT);
		}
		throw new DiscoveryException("Cannot find the required registry");
	}

	private String getRegistryType(String connectString, boolean ifType) {
		String[] typeAndHost = connectString.split("://");
		if(typeAndHost.length != 2){
			throw new RuntimeException("invalid url");
		}
		if(ifType){
			return typeAndHost[0];
		}else{
			return typeAndHost[1];
		}
	}
}
