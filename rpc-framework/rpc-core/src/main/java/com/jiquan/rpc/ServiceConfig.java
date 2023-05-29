package com.jiquan.rpc;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
public class ServiceConfig<T> {
	// the service interface that the provider has implemented
	private Class<T> interfaceProvider;
	// the instance of interface
	private Object ref;

	public Class<T> getInterface() {
		return interfaceProvider;
	}

	public void setInterface(Class<T> interfaceProvider) {
		this.interfaceProvider = interfaceProvider;
	}

	public Object getRef() {
		return ref;
	}

	public void setRef(Object ref) {
		this.ref = ref;
	}
}
