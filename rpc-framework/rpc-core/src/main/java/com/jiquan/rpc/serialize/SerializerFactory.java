package com.jiquan.rpc.serialize;

import com.jiquan.rpc.serialize.impl.HessianSerializer;
import com.jiquan.rpc.serialize.impl.JdkSerializer;
import com.jiquan.rpc.serialize.impl.JsonSerializer;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
public class SerializerFactory {
	private final static ConcurrentHashMap<String, SerializerWrapper> SERIALIZE_CACHE = new ConcurrentHashMap<>(8);
	private final static ConcurrentHashMap<Byte, SerializerWrapper> SERIALIZE_CODE_CACHE = new ConcurrentHashMap<>(8);


	static {
		SerializerWrapper jdk = new SerializerWrapper((byte) 1, "jdk", new JdkSerializer());
		SerializerWrapper json = new SerializerWrapper((byte) 2, "json", new JsonSerializer());
		SerializerWrapper hessian = new SerializerWrapper((byte) 3, "hessian", new HessianSerializer());

		SERIALIZE_CACHE.put("jdk", jdk);
		SERIALIZE_CACHE.put("json", json);
		SERIALIZE_CACHE.put("hessian", hessian);

		SERIALIZE_CODE_CACHE.put((byte) 1, jdk);
		SERIALIZE_CODE_CACHE.put((byte) 2, json);
		SERIALIZE_CODE_CACHE.put((byte) 3, hessian);
	}

	public static SerializerWrapper getSerializer(String serializeType) {
		return SERIALIZE_CACHE.get(serializeType);
	}

	public static SerializerWrapper getSerializer(byte serializeCode) {
		return SERIALIZE_CODE_CACHE.get(serializeCode);
	}
}
