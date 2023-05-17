package com.jiquan.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
public class ReferenceConfig<T> {
	private Class<T> interfaceRef;

	public ReferenceConfig() {
	}

	public void setInterface(Class<T> interfaceRef) {
		this.interfaceRef = interfaceRef;
	}

	public T get() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Class[] classes = new Class[]{interfaceRef};
		Object helloProxy = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				System.out.println("hello proxy");
				return null;
			}
		});
		return (T) helloProxy;
	}
}
