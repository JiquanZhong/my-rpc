package com.jiquan.rpc.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RpcResponse {
	// header
	private long requestId;
	private byte compressType;
	private byte serializeType;
	private long timeStamp;
	// 1: success 2: fail
	private byte code;
	// object of response
	private Object body;
}
