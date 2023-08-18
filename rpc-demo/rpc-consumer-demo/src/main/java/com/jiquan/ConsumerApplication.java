package com.jiquan;

import com.jiquan.api.HelloAPI;
import com.jiquan.api.HelloAPI2;
import com.jiquan.rpc.ReferenceConfig;
import com.jiquan.rpc.discovery.RegistryConfig;
import com.jiquan.rpc.RpcBootstrap;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class ConsumerApplication {
	public static void main(String[] args) {
		ReferenceConfig<HelloAPI2> reference = new ReferenceConfig<>();
		reference.setInterface(HelloAPI2.class);

		RpcBootstrap.getInstance()
				// registry the service in zookeeper
				.application("first-rpc-consumer")
				.registry(new RegistryConfig("zookeeper://10.188.78.86:2181,10.188.78.86:2182,10.188.78.86:2183"))
				.reference(reference);

		// get the proxy of service
		HelloAPI2 helloAPI = reference.get();

		while (true) {
			try {
				Thread.sleep(5000);
			} catch(InterruptedException e) {
				throw new RuntimeException(e);
			}

			List<String> res = helloAPI.sayHello("info of consumer");
			System.out.println(res);
		}
//		String sayHi = helloAPI.sayHello("This is parameter of method");
//		log.info("sayHi-->{}", sayHi);
	}
}
