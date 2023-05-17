package com.jiquan.rpc;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class RpcBootStrap {
	// singleton
	private static RpcBootStrap rpcBootStrap = new RpcBootStrap();

	private RpcBootStrap() {
	}

	public static RpcBootStrap getInstance() {
		return rpcBootStrap;
	}

	/**
	 * @param appName
	 * @return
	 */
	public RpcBootStrap application(String appName) {
		return this;
	}

	/**
	 * @param registryConfig
	 * @return
	 */
	public RpcBootStrap registry(RegistryConfig registryConfig) {
		return this;
	}

	public RpcBootStrap protocol(ProtocolConfig protocolConfig) {
		return this;
	}

	public RpcBootStrap start() {
		return this;
	}

	public RpcBootStrap publish(ServiceConfig service) {
		return this;
	}

	public RpcBootStrap publish(List<ServiceConfig> services) {
		return this;
	}

	public RpcBootStrap reference(ReferenceConfig<?> reference) {
		return this;
	}
}
