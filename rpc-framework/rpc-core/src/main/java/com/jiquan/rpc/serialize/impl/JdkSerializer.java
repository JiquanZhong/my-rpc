package com.jiquan.rpc.serialize.impl;

import com.jiquan.exceptions.SerializeException;
import com.jiquan.rpc.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class JdkSerializer implements Serializer {
	@Override
	public byte[] serialize(Object object) {
		if(object == null) return null;

		try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream outputStream = new ObjectOutputStream(baos);) {
			outputStream.writeObject(object);

			byte[] result = baos.toByteArray();
			if(log.isDebugEnabled()) log.debug("the Object {} is serialized, the result byte array length is {}", object, result.length);
			return result;
		} catch(IOException e) {
			log.error("error when serializing the {}", object);
			throw new SerializeException(e);
		}
	}

	@Override
	public <T> T deserialize(byte[] bytes, Class<T> clazz) {
		if(bytes == null || clazz == null) return null;

		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			ObjectInputStream objectInputStream = new ObjectInputStream(bais);
			T result = (T)objectInputStream.readObject();
			if(log.isDebugEnabled()) log.debug("the Object of {} is deserialized, the result Object is {}", clazz, result);
			return result;
		}catch(IOException | ClassNotFoundException e){
			log.error("error when deserializing the object of {}", clazz);
			throw new SerializeException(e);
		}
	}


}
