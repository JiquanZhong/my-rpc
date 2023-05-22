package com.jiquan.rpc;

import com.jiquan.rpc.discovery.Registry;
import com.jiquan.rpc.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class RpcBootStrap {
	// singleton
	private static final RpcBootStrap rpcBootStrap = new RpcBootStrap();
	private String appName = "default name";
	private RegistryConfig registryConfig;
	private ProtocolConfig protocolConfig;
	private int port = 8088;
	private Registry registry;

	// Maintain published and exposed service list key-> fully qualified name of interface value -> ServiceConfig
	private static final Map<String, ServiceConfig<?>> SERVICE_LIST = new ConcurrentHashMap<>(16);

	private RpcBootStrap() {
		// TODO initialization
	}

	public static RpcBootStrap getInstance() {
		return rpcBootStrap;
	}

	/**
	 * @param appName
	 * @return
	 */
	public RpcBootStrap application(String appName) {
		this.appName = appName; return this;
	}

	/**
	 * Used to configure a registry
	 *
	 * @param registryConfig registration center
	 * @return this current instance
	 */
	public RpcBootStrap registry(RegistryConfig registryConfig) {
		// A zookeeper instance is maintained here, but if written in this way, zookeeper will be coupled with the current project
		// We actually hope that we can expand more different implementations in the future
		// Try to use registryConfig to get a registration center, which is a bit of a factory design pattern
		this.registry = registryConfig.getRegistry(); return this;
	}

	/**
	 * Configure the protocol used by the currently exposed service
	 * @param protocolConfig protocol encapsulation
	 * @return this current instance
	 */
	public RpcBootStrap protocol(ProtocolConfig protocolConfig) {
		this.protocolConfig = protocolConfig;
		if(log.isDebugEnabled()) log.debug("Current app is using {} for serialization", protocolConfig.toString());
		return this;
	}

	public void start() {
		try {
			Thread.sleep(1000000000);
		} catch(InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Publish the service, implement the interface and register it with the service center
	 * @param service encapsulated service that needs to be published
	 * @return this current instance
	 */
	public RpcBootStrap publish(ServiceConfig service) {
		registry.register(service);
		SERVICE_LIST.put(service.getInterface().getName(), service);
		return this;
	}

	public RpcBootStrap publish(List<ServiceConfig> services) {
		for(ServiceConfig<?> service : services) {
			this.publish(service);
		}
		return this;
	}

	public RpcBootStrap reference(ReferenceConfig<?> reference) {
		// In this method, can we get the relevant configuration items - registration center
		// Configure the reference to facilitate generation of proxy objects when the get method is called in the future
		// 1. The reference needs a registration center
		reference.setRegistry(registry); return this;
	}
}
