package com.jiquan.rpc.channelhandler.handler;

import com.jiquan.rpc.compress.Compressor;
import com.jiquan.rpc.compress.CompressorFactory;
import com.jiquan.rpc.serialize.Serializer;
import com.jiquan.rpc.serialize.SerializerFactory;
import com.jiquan.rpc.transport.message.MessageFormatConstant;
import com.jiquan.rpc.transport.message.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Random;

/**
 * <pre>
 *   0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22
 *   +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
 *   |    magic          |ver |head  len|    full length    |code  ser|comp|              RequestId                |
 *   +-----+-----+-------+----+----+----+----+-----------+----- ---+--------+----+----+----+----+----+----+---+---+
 *   |                                                                                                             |
 *   |                                         body                                                                |
 *   |                                                                                                             |
 *   +--------------------------------------------------------------------------------------------------------+---+
 * </pre>
 *
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class RpcResponseEncoder extends MessageToByteEncoder<RpcResponse> {

	@Override
	protected void encode(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse, ByteBuf byteBuf) throws Exception {
		byteBuf.writeBytes(MessageFormatConstant.MAGIC);
		byteBuf.writeByte(MessageFormatConstant.VERSION);
		byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
		byteBuf.writerIndex(byteBuf.writerIndex() + MessageFormatConstant.FULL_FIELD_LENGTH);
		byteBuf.writeByte(rpcResponse.getCode());
		byteBuf.writeByte(rpcResponse.getSerializeType());
		byteBuf.writeByte(rpcResponse.getCompressType());
		byteBuf.writeLong(rpcResponse.getRequestId());
		byteBuf.writeLong(rpcResponse.getTimeStamp());

		byte[] body = null;

		if(rpcResponse.getBody() != null){
			// Serialize the response
			body = SerializerFactory.getSerializer(rpcResponse.getSerializeType())
					.getSerializer()
					.serialize(rpcResponse.getBody());

			// compress
			Compressor compressor = CompressorFactory.getCompressor(rpcResponse.getCompressType()).getCompressor();
			body = compressor.compress(body);

			if(log.isDebugEnabled()) {
				log.debug("Compress of response [{}] is done", rpcResponse.getRequestId());
			}
		}

		if(body != null) {
			byteBuf.writeBytes(body);
		}
		int bodyLength = body == null ? 0 : body.length;

		int writerIndex = byteBuf.writerIndex();
		byteBuf.writerIndex(MessageFormatConstant.MAGIC.length
									+ MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH
						   );
		byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyLength);
		byteBuf.writerIndex(writerIndex);

		if(log.isDebugEnabled()) {
			log.debug("The response [{}] is encoded", rpcResponse.getRequestId());
		}

		// network delay
		Thread.sleep((long) (50L * Math.random()));
	}
}
