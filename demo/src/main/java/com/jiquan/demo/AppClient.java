package com.jiquan.demo;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * @author ZHONG Jiquan
 * @create 08/08/2023 - 04:13
 */
public class AppClient implements Serializable{
	public void run() throws InterruptedException {
		// 定义线程池
		NioEventLoopGroup group = new NioEventLoopGroup();
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(group)
				.remoteAddress(new InetSocketAddress(8081))
				//初始化一个什么样的channel
				.channel(NioSocketChannel.class)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel socketChannel) throws Exception {
						socketChannel.pipeline().addLast(new MyChannelHandler2());
					}
				});
		//尝试连接服务器
		ChannelFuture channelFuture = bootstrap.connect().sync();//同步等待
		//获取channel，并写出数据
		channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer("hello netty".getBytes(StandardCharsets.UTF_8)));
		//阻塞程序，等待接收future
		ChannelFuture sync = channelFuture.channel().closeFuture().sync();
		sync.channel().closeFuture().sync();
	}

	public static void main(String[] args) throws InterruptedException {
		new AppClient().run();
	}
}
