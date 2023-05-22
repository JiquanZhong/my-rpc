package com.jiquan.rpc;

import com.jiquan.rpc.discovery.Registry;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class ReferenceConfig<T> {
	private Class<T> interfaceRef;

	private Registry registry;

	public void setInterface(Class<T> interfaceRef) {
		this.interfaceRef = interfaceRef;
	}

	public T get() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Class[] classes = new Class[]{interfaceRef};
		Object helloProxy = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				log.info("method-->{}",method.getName());
				log.info("args-->{}", args);

				InetSocketAddress address = registry.lookup(interfaceRef.getName());
				if(log.isDebugEnabled()) log.debug("find the target service of {} {}", interfaceRef.getName(), address);
				return null;
			};

		});
		return (T) helloProxy;
	}

	public Class<T> getInterfaceRef() {
		return interfaceRef;
	}

	public void setInterfaceRef(Class<T> interfaceRef) {
		this.interfaceRef = interfaceRef;
	}

	public Registry getRegistry() {
		return registry;
	}

	public void setRegistry(Registry registry) {
		this.registry = registry;
	}
}
