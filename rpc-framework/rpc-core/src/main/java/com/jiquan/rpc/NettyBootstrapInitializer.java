package com.jiquan.rpc;

import com.jiquan.rpc.channelhandler.ConsumerChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class NettyBootstrapInitializer {
	private static final Bootstrap BOOTSTRAP = new Bootstrap();

	static {
		NioEventLoopGroup group = new NioEventLoopGroup();
		BOOTSTRAP.group(group)
				.channel(NioSocketChannel.class)
				.handler(new ConsumerChannelInitializer());
	}

	private NettyBootstrapInitializer(){}

	public static Bootstrap getBootstrap(){
		return BOOTSTRAP;
	}
}
