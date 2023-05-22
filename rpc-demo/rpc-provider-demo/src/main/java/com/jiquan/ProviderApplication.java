package com.jiquan;

import com.jiquan.api.HelloAPI;
import com.jiquan.impl.HelloImplAPI;
import com.jiquan.rpc.ProtocolConfig;
import com.jiquan.rpc.discovery.RegistryConfig;
import com.jiquan.rpc.RpcBootStrap;
import com.jiquan.rpc.ServiceConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class ProviderApplication {
	public static void main(String[] args) {
		ServiceConfig<HelloAPI> service = new ServiceConfig<>();
		service.setInterface(HelloAPI.class);
		service.setRef(new HelloImplAPI());

		RpcBootStrap.getInstance()
				// registry the service in zookeeper
				.registry(new RegistryConfig("zookeeper://10.188.78.86:2181,10.188.78.86:2182,10.188.78.86:2183"))
				// put the configuration online
				.protocol(new ProtocolConfig("jdk"))
				// initialize the service
				.publish(service)
				// start the service
				.start();
	}
}
