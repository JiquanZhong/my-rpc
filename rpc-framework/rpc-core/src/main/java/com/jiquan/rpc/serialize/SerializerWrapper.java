package com.jiquan.rpc.serialize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SerializerWrapper {
	private byte code;
	private String type;
	private Serializer serializer;
}
