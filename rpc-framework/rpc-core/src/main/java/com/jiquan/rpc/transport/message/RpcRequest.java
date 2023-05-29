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
public class RpcRequest {

	// 请求的id
	private long requestId;

	// 请求的类型，压缩的类型，序列化的方式
	private byte requestType;
	private byte compressType;
	private byte serializeType;

	// 具体的消息体
	private RequestPayload requestPayload;

}

