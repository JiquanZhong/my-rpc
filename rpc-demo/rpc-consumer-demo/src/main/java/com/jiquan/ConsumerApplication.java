package com.jiquan;

import com.jiquan.api.HelloAPI;
import com.jiquan.rpc.ReferenceConfig;
import com.jiquan.rpc.discovery.RegistryConfig;
import com.jiquan.rpc.RpcBootStrap;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class ConsumerApplication {
	public static void main(String[] args) {
		ReferenceConfig<HelloAPI> reference = new ReferenceConfig<>();
		reference.setInterface(HelloAPI.class);

		RpcBootStrap.getInstance()
				// registry the service in zookeeper
				.application("first-rpc-consumer")
				.registry(new RegistryConfig("zookeeper://10.188.78.86:2181,10.188.78.86:2182,10.188.78.86:2183"))
				.reference(reference);

		// get the proxy of service
		HelloAPI helloService = reference.get();
		helloService.sayHello("ConsumerApplication is saying hello");
	}
}
