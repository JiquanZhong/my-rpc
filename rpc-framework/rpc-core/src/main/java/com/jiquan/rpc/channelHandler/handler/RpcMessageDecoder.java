package com.jiquan.rpc.channelHandler.handler;

import com.jiquan.rpc.transport.message.MessageFormatConstant;
import com.jiquan.rpc.transport.message.RequestPayload;
import com.jiquan.rpc.transport.message.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder{
	public RpcMessageDecoder() {
		super(
				// Find the total length of the current message, intercept the message, and we can analyze the intercepted message
				// The maximum frame length, if it exceeds this maxFrameLength value, it will be discarded directly
				MessageFormatConstant.MAX_FRAME_LENGTH,
				// The offset of the length field,
				MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH,
				// the length of the length field
				MessageFormatConstant.FULL_FIELD_LENGTH,
				// todo 负载的适配长度
				-(MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH
						+ MessageFormatConstant.HEADER_FIELD_LENGTH + MessageFormatConstant.FULL_FIELD_LENGTH),
				0);
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
		Object decode = super.decode(ctx, in);
		if(decode instanceof ByteBuf byteBuf){
			return decodeFrame(byteBuf);
		}
		return null;
	}

	private Object decodeFrame(ByteBuf byteBuf) {
		// 1. decode magic
		byte[] magic = new byte[MessageFormatConstant.MAGIC.length];
		byteBuf.readBytes(magic);
		// verify the magic
		for (int i = 0; i < magic.length; i++) {
			if(magic[i] != MessageFormatConstant.MAGIC[i]){
				throw new RuntimeException("The request obtained is not legitimate。");
			}
		}

		byte version = byteBuf.readByte();
		if(version > MessageFormatConstant.VERSION){
			throw new RuntimeException("The requested version is not supported.");
		}

		short headLength = byteBuf.readShort();
		int fullLength = byteBuf.readInt();
		// todo 判断是不是心跳检测
		byte requestType = byteBuf.readByte();
		byte serializeType = byteBuf.readByte();
		byte compressType = byteBuf.readByte();
		long requestId = byteBuf.readLong();

		// wrapping
		RpcRequest rpcRequest = new RpcRequest();
		rpcRequest.setRequestType(requestType);
		rpcRequest.setCompressType(compressType);
		rpcRequest.setSerializeType(serializeType);

		// todo 心跳请求没有负载，此处可以判断并直接返回
		if( requestType == 2 ){
			return rpcRequest;
		}

		int payloadLength = fullLength - headLength;
		byte[] payload = new byte[payloadLength];
		byteBuf.readBytes(payload);
		// todo 解压缩

		// todo 反序列化
		try (ByteArrayInputStream bis = new ByteArrayInputStream(payload);
			 ObjectInputStream ois = new ObjectInputStream(bis)
		) {
			RequestPayload requestPayload = (RequestPayload) ois.readObject();
			rpcRequest.setRequestPayload(requestPayload);
		} catch (IOException | ClassNotFoundException e){
			log.error("An exception occurred while requesting [{}] deserialization",requestId,e);
		}

		return rpcRequest;
	}
}
