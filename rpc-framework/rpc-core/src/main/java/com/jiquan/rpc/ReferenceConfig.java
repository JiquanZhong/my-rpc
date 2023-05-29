package com.jiquan.rpc;

import com.jiquan.rpc.discovery.Registry;
import com.jiquan.rpc.proxy.handler.RpcConsumerInvocationHandler;
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
		Class<T>[] classes = new Class[]{interfaceRef};
		InvocationHandler handler = new RpcConsumerInvocationHandler(registry, interfaceRef);
		Object proxy = Proxy.newProxyInstance(classLoader, classes, handler);
		return (T) proxy;
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
