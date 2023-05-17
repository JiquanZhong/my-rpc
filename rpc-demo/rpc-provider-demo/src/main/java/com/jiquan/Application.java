package com.jiquan;

import com.jiquan.api.HelloAPI;
import com.jiquan.impl.HelloImplAPI;
import com.jiquan.rpc.ProtocolConfig;
import com.jiquan.rpc.RegistryConfig;
import com.jiquan.rpc.RpcBootStrap;
import com.jiquan.rpc.ServiceConfig;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
public class Application {
	public static void main(String[] args) {
		ServiceConfig<HelloAPI> service = new ServiceConfig<>();
		service.setInterface(HelloAPI.class);
		service.setRef(new HelloImplAPI());

		RpcBootStrap.getInstance()
				// registry the service in zookeeper
				.registry(new RegistryConfig("10.188.78.86:2181,10.188.78.86:2182,10.188.78.86:2183"))
				// put the configuration online
				.protocol(new ProtocolConfig(""))
				// initialize the service
				.publish(service)
				// start the service
				.start();
	}
}
