package com.jiquan.impl;

import com.jiquan.api.HelloAPI;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
public class HelloImplAPI implements HelloAPI {
	@Override
	public String sayHello(String msg) {
		return "hi consumer: " + msg;
	}
}
