package com.jiquan.rpc.channelHandler;

import com.jiquan.rpc.channelHandler.handler.MySimpleChannelInboundHandler;
import com.jiquan.rpc.channelHandler.handler.RpcMessageEncoder;
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
				.addLast(new RpcMessageEncoder())
				.addLast(new MySimpleChannelInboundHandler());
	}
}
