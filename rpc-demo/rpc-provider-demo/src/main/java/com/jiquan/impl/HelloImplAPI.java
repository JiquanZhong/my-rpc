package com.jiquan.impl;

import com.jiquan.api.HelloAPI;
import com.jiquan.rpc.annotation.RpcAPI;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@RpcAPI
public class HelloImplAPI implements HelloAPI {
	@Override
	public List<String> sayHello(String msg) {
		List<String> list = new ArrayList<>();
		list.add("provider");
		list.add(msg);
		return list;
	}
}
