package com.jiquan.rpc.channelhandler.handler;

import com.jiquan.rpc.compress.Compressor;
import com.jiquan.rpc.compress.CompressorFactory;
import com.jiquan.rpc.enumeration.RequestType;
import com.jiquan.rpc.serialize.Serializer;
import com.jiquan.rpc.serialize.SerializerFactory;
import com.jiquan.rpc.transport.message.MessageFormatConstant;
import com.jiquan.rpc.transport.message.RequestPayload;
import com.jiquan.rpc.transport.message.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * * <pre>
 *  *   0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22
 *  *   +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
 *  *   |    magic          |ver |head  len|    full length    | qt | ser|comp|              RequestId                |
 *  *   +-----+-----+-------+----+----+----+----+-----------+----- ---+--------+----+----+----+----+----+----+---+---+
 *  *   |                                                                                                             |
 *  *   |                                         body                                                                |
 *  *   |                                                                                                             |
 *  *   +--------------------------------------------------------------------------------------------------------+---+
 *  * </pre>
 *
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class RpcRequestDecoder extends LengthFieldBasedFrameDecoder {
	public RpcRequestDecoder() {
		super(
				// Find the total length of the current message, intercept the message, and we can analyze the intercepted message
				// The maximum frame length, if it exceeds this maxFrameLength value, it will be discarded directly
				MessageFormatConstant.MAX_FRAME_LENGTH,
				// The offset of the length field,
				MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH,
				// the length of the length field
				MessageFormatConstant.FULL_FIELD_LENGTH,
				// todo 负载的适配长度
				-(MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH + MessageFormatConstant.FULL_FIELD_LENGTH), 0);
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
		Object decode = super.decode(ctx, in);
		if(decode instanceof ByteBuf byteBuf) {
			return decodeFrame(byteBuf);
		} return null;
	}

	private Object decodeFrame(ByteBuf byteBuf) {
		// 1. decode magic
		byte[] magic = new byte[MessageFormatConstant.MAGIC.length]; byteBuf.readBytes(magic);
		// verify the magic
		for(int i = 0; i < magic.length; i++) {
			if(magic[i] != MessageFormatConstant.MAGIC[i]) {
				throw new RuntimeException("The request obtained is not legitimate。");
			}
		}

		byte version = byteBuf.readByte();
		if(version > MessageFormatConstant.VERSION) {
			throw new RuntimeException("The requested version is not supported.");
		}

		short headLength = byteBuf.readShort();
		int fullLength = byteBuf.readInt();
		byte requestType = byteBuf.readByte();
		byte serializeType = byteBuf.readByte();
		byte compressType = byteBuf.readByte();
		long requestId = byteBuf.readLong();

		// wrapping
		RpcRequest rpcRequest = new RpcRequest();
		rpcRequest.setRequestType(requestType);
		rpcRequest.setCompressType(compressType);
		rpcRequest.setSerializeType(serializeType);
		rpcRequest.setRequestId(requestId);

		if(requestType == RequestType.HEARTBEAT.getId()) {
			return rpcRequest;
		}

		int payloadLength = fullLength - headLength;
		byte[] payload = new byte[payloadLength];
		byteBuf.readBytes(payload);

		// decompress
		Compressor compressor = CompressorFactory.getCompressor(rpcRequest.getCompressType()).getCompressor();
		payload = compressor.decompress(payload);

		if(log.isDebugEnabled()) {
			log.debug("the request [{}] is decompressed", rpcRequest.getRequestId());
		}

		// deserialization
		Serializer serializer = SerializerFactory.getSerializer(serializeType).getSerializer();
		RequestPayload requestPayload = serializer.deserialize(payload, RequestPayload.class);
		rpcRequest.setRequestPayload(requestPayload);

		if(log.isDebugEnabled()) {
			log.debug("the request [{}] is decoded", rpcRequest.getRequestId());
		}

		return rpcRequest;
	}
}
