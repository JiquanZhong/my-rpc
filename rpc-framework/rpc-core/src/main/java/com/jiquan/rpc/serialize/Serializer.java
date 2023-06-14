package com.jiquan.rpc.serialize;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
public interface Serializer {
	/**
	 * Abstract method for serialization
	 * @param object the object instance to be serialized
	 * @return byte array
	 */
	byte[] serialize(Object object);
	/**
	 * The method of deserialization
	 * @param bytes The byte array to be deserialized
	 * @param clazz the class object of the target class
	 * @param <T> target class generic
	 * @return target instance
	 */
	<T> T deserialize(byte[] bytes, Class<T> clazz);
}
