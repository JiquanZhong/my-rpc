package com.jiquan.rpc.channelHandler.handler;

import com.jiquan.rpc.serialize.Serializer;
import com.jiquan.rpc.serialize.SerializerFactory;
import com.jiquan.rpc.transport.message.MessageFormatConstant;
import com.jiquan.rpc.transport.message.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * * <p>
 * * <pre>
 * *   0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22
 * *   +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
 * *   |    magic          |ver |head  len|    full length    | code | ser|comp|              RequestId                |
 * *   +-----+-----+-------+----+----+----+----+-----------+----- ---+--------+----+----+----+----+----+----+---+---+
 * *   |                                                                                                             |
 * *   |                                         body                                                                |
 * *   |                                                                                                             |
 * *   +--------------------------------------------------------------------------------------------------------+---+
 * * </pre>
 * *
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class RpcResponseDecoder extends LengthFieldBasedFrameDecoder {
	public RpcResponseDecoder() {
		super(
				MessageFormatConstant.MAX_FRAME_LENGTH,
				MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH,
				MessageFormatConstant.FULL_FIELD_LENGTH,
				-(MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH
						+ MessageFormatConstant.HEADER_FIELD_LENGTH + MessageFormatConstant.FULL_FIELD_LENGTH),
				0);
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
		Object decode = super.decode(ctx, in);
		if(decode instanceof ByteBuf byteBuf) {
			return decodeFrame(byteBuf);
		}
		return null;
	}


	private Object decodeFrame(ByteBuf byteBuf) {
		byte[] magic = new byte[MessageFormatConstant.MAGIC.length];
		byteBuf.readBytes(magic);
		for(int i = 0; i < magic.length; i++) {
			if(magic[i] != MessageFormatConstant.MAGIC[i]) {
				throw new RuntimeException("The request obtained is not legitimate。");
			}
		}

		byte version = byteBuf.readByte();
		if(version > MessageFormatConstant.VERSION) {
			throw new RuntimeException("The request version is not supported");
		}

		short headLength = byteBuf.readShort();

		int fullLength = byteBuf.readInt();

		byte responseCode = byteBuf.readByte();

		byte serializeType = byteBuf.readByte();

		byte compressType = byteBuf.readByte();

		long requestId = byteBuf.readLong();

		RpcResponse rpcResponse = new RpcResponse();
		rpcResponse.setCode(responseCode);
		rpcResponse.setCompressType(compressType);
		rpcResponse.setSerializeType(serializeType);
		rpcResponse.setRequestId(requestId);

		// todo 心跳请求没有负载，此处可以判断并直接返回
//        if( requestType == RequestType.HEART_BEAT.getId()){
//            return yrpcRequest;
//        }

		int bodyLength = fullLength - headLength;
		byte[] payload = new byte[bodyLength];
		byteBuf.readBytes(payload);

		// todo 解压缩

		Serializer serializer = SerializerFactory
				.getSerializer(rpcResponse.getSerializeType()).getSerializer();
		Object body = serializer.deserialize(payload, Object.class);
		rpcResponse.setBody(body);

		if(log.isDebugEnabled()) {
			log.debug("The response {} is decoded", rpcResponse.getRequestId());
		}

		return rpcResponse;
	}
}
