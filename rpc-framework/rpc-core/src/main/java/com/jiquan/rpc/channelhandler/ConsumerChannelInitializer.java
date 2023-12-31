package com.jiquan.rpc.channelhandler;

import com.jiquan.rpc.channelhandler.handler.MySimpleChannelInboundHandler;
import com.jiquan.rpc.channelhandler.handler.RpcRequestEncoder;
import com.jiquan.rpc.channelhandler.handler.RpcResponseDecoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ch.pipeline()
				.addLast(new LoggingHandler(LogLevel.DEBUG))
				.addLast(new RpcRequestEncoder())
				.addLast(new RpcResponseDecoder())
				.addLast(new MySimpleChannelInboundHandler());
	}
}
