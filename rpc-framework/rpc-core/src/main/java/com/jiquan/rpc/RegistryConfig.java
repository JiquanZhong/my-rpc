package com.jiquan.rpc;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
public class RegistryConfig {
	private String connectString;

	public RegistryConfig() {
	}

	public RegistryConfig(String connectString) {
		this.connectString = connectString;
	}

	public String getConnectString() {
		return connectString;
	}

	public void setConnectString(String connectString) {
		this.connectString = connectString;
	}
}
