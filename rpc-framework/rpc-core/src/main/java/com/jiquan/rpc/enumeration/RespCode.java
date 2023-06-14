package com.jiquan.rpc.enumeration;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
public enum RespCode {
	SUCCESS((byte) 1,"Success"), FAIL((byte)2,"Fail");

	private byte code;
	private String desc;

	RespCode(byte code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public byte getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
}
