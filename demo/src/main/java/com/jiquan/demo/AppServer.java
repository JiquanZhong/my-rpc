package com.jiquan.demo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author ZHONG Jiquan
 * @create 08/08/2023 - 04:20
 */
public class AppServer {
	private int port;

	public AppServer(int port) {
		this.port = port;
	}

	public void start() {
		// 创建EventLoop
		NioEventLoopGroup boss = null;
		NioEventLoopGroup worker = null;

		try {
			boss = new NioEventLoopGroup(2);
			worker = new NioEventLoopGroup(5);
			//需要服务器引导程序
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			//配置服务器
			serverBootstrap.group(boss, worker)
					.channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel socketChannel) throws Exception {
							socketChannel.pipeline().addLast(new MyChannelHandler());
						}
					});
			ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
			System.out.println(channelFuture.channel().closeFuture().sync());
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			boss.shutdownGracefully();
			worker.shutdownGracefully();
		}
	}

	public static void main(String[] args) {
		new AppServer(8081).start();
	}
}
