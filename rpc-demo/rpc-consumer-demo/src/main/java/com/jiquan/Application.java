package com.jiquan;

import com.jiquan.api.HelloAPI;
import com.jiquan.rpc.ProtocolConfig;
import com.jiquan.rpc.ReferenceConfig;
import com.jiquan.rpc.RegistryConfig;
import com.jiquan.rpc.RpcBootStrap;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
public class Application {
	public static void main(String[] args) {
		ReferenceConfig<HelloAPI> reference = new ReferenceConfig<>();
		reference.setInterface(HelloAPI.class);

		RpcBootStrap.getInstance()
				// registry the service in zookeeper
				.application("first-rpc-consumer")
				.registry(new RegistryConfig())
				.reference(reference);

		// get the proxy of service
		HelloAPI helloService = reference.get();
		helloService.sayHello("Hello RPC");
	}
}
