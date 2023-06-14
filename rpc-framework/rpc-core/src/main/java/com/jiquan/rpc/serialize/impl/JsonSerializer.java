package com.jiquan.rpc.serialize.impl;

import com.alibaba.fastjson2.JSON;
import com.jiquan.rpc.serialize.Serializer;
import com.jiquan.rpc.transport.message.RequestPayload;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class JsonSerializer implements Serializer {
	@Override
	public byte[] serialize(Object object) {
		if(object == null) return null;

		byte[] result = JSON.toJSONBytes(object);
		if(log.isDebugEnabled()) log.debug("the Object {} is serialized, the result byte array length is {}", object, result.length);
		return result;
	}

	@Override
	public <T> T deserialize(byte[] bytes, Class<T> clazz) {
		if(bytes == null || clazz == null) return null;

		T result = JSON.parseObject(bytes, clazz);
		if(log.isDebugEnabled()) log.debug("the Object of {} is deserialized, the result Object is {}", clazz, result);
		return result;
	}

	// testing
	public static void main(String[] args) {
		Serializer serializer = new JsonSerializer();

		RequestPayload requestPayload = new RequestPayload();
		requestPayload.setInterfaceName("xxxx");
		requestPayload.setMethodName("yyy");
		requestPayload.setParametersValue(new Object[]{"xxxx"});

		byte[] serialize = serializer.serialize(requestPayload);
		System.out.println(Arrays.toString(serialize));

		RequestPayload deserialize = serializer.deserialize(serialize, RequestPayload.class);
		System.out.println(deserialize);
	}
}
