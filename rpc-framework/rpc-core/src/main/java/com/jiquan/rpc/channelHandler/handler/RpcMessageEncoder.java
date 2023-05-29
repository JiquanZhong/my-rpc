package com.jiquan.rpc.channelHandler.handler;

import com.jiquan.rpc.transport.message.MessageFormatConstant;
import com.jiquan.rpc.transport.message.RequestPayload;
import com.jiquan.rpc.transport.message.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 *  0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22
 * +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
 * |    magic          |ver |head  len|    full length    | qt | ser|comp|              RequestId                |
 * +-----+-----+-------+----+----+----+----+-----------+----- ---+--------+----+----+----+----+----+----+---+---+
 * |                                                                                                             |
 * |                                         body                                                                |
 * |                                                                                                             |
 * +--------------------------------------------------------------------------------------------------------+---+
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcRequest> {
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
		byteBuf.writerIndex(byteBuf.writerIndex() + 4);
		// 3 types should be filled
		byteBuf.writeByte(rpcRequest.getRequestType());
		byteBuf.writeByte(rpcRequest.getSerializeType());
		byteBuf.writeByte(rpcRequest.getCompressType());
		// 8 bytes request id
		byteBuf.writeLong(rpcRequest.getRequestId());
		// fill the request body
		byte[] body = getBodyBytes(rpcRequest.getRequestPayload());
		byteBuf.writeBytes(body);
		// mark the current writer pointer
		int writerIndex = byteBuf.writerIndex();
		byteBuf.writerIndex(7);
		byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + body.length);
		// move the writer pointer back to the marked position
		byteBuf.writerIndex(writerIndex);
	}

	private byte[] getBodyBytes(RequestPayload requestPayload) {
		// TODO 针对不同的消息类型需要做不同的处理，心跳的请求，没有payload
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream outputStream = new ObjectOutputStream(baos);
			outputStream.writeObject(requestPayload);

			// TODO compress
			return baos.toByteArray();
		} catch (IOException e) {
			log.error("error when serializing the msg");
			throw new RuntimeException(e);
		}
	}
}
