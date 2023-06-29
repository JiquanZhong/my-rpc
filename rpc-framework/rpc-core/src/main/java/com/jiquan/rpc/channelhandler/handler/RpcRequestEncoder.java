package com.jiquan.rpc.channelhandler.handler;

import com.jiquan.rpc.compress.Compressor;
import com.jiquan.rpc.compress.CompressorFactory;
import com.jiquan.rpc.serialize.Serializer;
import com.jiquan.rpc.serialize.SerializerFactory;
import com.jiquan.rpc.transport.message.MessageFormatConstant;
import com.jiquan.rpc.transport.message.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * <pre>
 *  0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22
 * +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
 * |    magic          |ver |head  len|    full length    | qt | ser|comp|              RequestId                |
 * +-----+-----+-------+----+----+----+----+-----------+----- ---+--------+----+----+----+----+----+----+---+---+
 * |                                                                                                             |
 * |                                         body                                                                |
 * |                                                                                                             |
 * +--------------------------------------------------------------------------------------------------------+---+
 * </pre>
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class RpcRequestEncoder extends MessageToByteEncoder<RpcRequest> {
	@Override
	protected void encode(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest, ByteBuf byteBuf) throws Exception {
		// 4 byte MAGIC
		byteBuf.writeBytes(MessageFormatConstant.MAGIC);
		// 1 byte VERSION
		byteBuf.writeByte(MessageFormatConstant.VERSION);
		// 2 byte header length
		byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
		// cannot deal with the final length of msg, move the writer pointer forward for 4 bytes
		// fill this field later
		byteBuf.writerIndex(byteBuf.writerIndex() + MessageFormatConstant.FULL_FIELD_LENGTH);
		// 3 types should be filled
		byteBuf.writeByte(rpcRequest.getRequestType());
		byteBuf.writeByte(rpcRequest.getSerializeType());
		byteBuf.writeByte(rpcRequest.getCompressType());
		// 8 bytes request id
		byteBuf.writeLong(rpcRequest.getRequestId());
		// fill the request body
		Serializer serializer = SerializerFactory.getSerializer(rpcRequest.getSerializeType()).getSerializer();
		byte[] body = serializer.serialize(rpcRequest.getRequestPayload());

		Compressor compressor = CompressorFactory.getCompressor(rpcRequest.getCompressType()).getCompressor();
		body = compressor.compress(body);
		if(log.isDebugEnabled()){
			log.debug("Compress of request [{}] is done",rpcRequest.getRequestId());
		}

		if(body != null) byteBuf.writeBytes(body);

		int bodyLength = body == null ? 0 : body.length;
		// mark the current writer pointer
		int writerIndex = byteBuf.writerIndex();
		byteBuf.writerIndex(MessageFormatConstant.MAGIC.length
						   + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH
						   );
		byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyLength);
		// move the writer pointer back to the marked position
		byteBuf.writerIndex(writerIndex);

		if(log.isDebugEnabled()){
			log.debug("Encapsulation of request [{}] is done",rpcRequest.getRequestId());
		}
	}
}
