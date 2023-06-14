package com.jiquan.rpc.serialize.impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.jiquan.exceptions.SerializeException;
import com.jiquan.rpc.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class HessianSerializer implements Serializer {
	@Override
	public byte[] serialize(Object object) {
		if(object == null) return null;

		try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			Hessian2Output hessian2Output = new Hessian2Output(baos);
			hessian2Output.writeObject(object);
			hessian2Output.flush();
			byte[] result = baos.toByteArray();
			if(log.isDebugEnabled()) {
				log.debug("the object {} is serialized, the length of byte array is {}", object, result.length);
			}
			return result;
		} catch(IOException e) {
			log.error("error when using hessian to serialize {}.", object);
			throw new SerializeException(e);
		}
	}

	@Override
	public <T> T deserialize(byte[] bytes, Class<T> clazz) {
		if(bytes == null || clazz == null) return null;

		try(ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
			Hessian2Input hessian2Input = new Hessian2Input(bais);
			T t = (T) hessian2Input.readObject();
			if(log.isDebugEnabled()){
				log.debug("the object of {} is deserialized with Hessian.",clazz);
			}
			return t;
		} catch(IOException e) {
			log.error("error when deserializing the object of {}", clazz);
			throw new SerializeException();
		}
	}
}
