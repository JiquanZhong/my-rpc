package com.jiquan.rpc.enumeration;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
public enum RequestType {
	REQUEST((byte)1, "normal request"),
	HEARTBEAT((byte)2, "heart beat request");

	private byte id;
	private String type;

	RequestType(byte id, String type) {
		this.id = id;
		this.type = type;
	}

	public byte getId() {
		return id;
	}

	public String getType() {
		return type;
	}
}
