package com.jiquan.rpc;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
public class ServiceConfig<T> {
	// the service interface that the provider has implemented
	private Class<?> interfaceProvider;
	// the instance of interface
	private Object ref;

	public Class<?> getInterface() {
		return interfaceProvider;
	}

	public void setInterface(Class<?> interfaceProvider) {
		this.interfaceProvider = interfaceProvider;
	}

	public Object getRef() {
		return ref;
	}

	public void setRef(Object ref) {
		this.ref = ref;
	}
}
