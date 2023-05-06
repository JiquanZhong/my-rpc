package com.jiquan.demo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import java.nio.charset.StandardCharsets;

/**
 * @author ZHONG Jiquan
 * @create 06/08/2023 - 01:32
 */
// 为了线程安全
@ChannelHandler.Sharable
public class MyChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {
	/**
	 * 处理接收到的消息
	 * @param channelHandlerContext 通道上下文，代指Channel
	 * @param byteBuf 字节序列，通过ByteBuf操作基础的字节数组和缓冲区，因为JDK原生操作字节麻烦、效率低
	 * @throws Exception
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
		System.out.println("接收到的消息："+byteBuf.toString(CharsetUtil.UTF_8));
		channelHandlerContext.channel().writeAndFlush(Unpooled.copiedBuffer("ACK".getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 *
	 * @param ctx 通道上下文，代指Channel
	 * @param cause 发生的异常
	 * @throws Exception
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		/**
		 * @Description 处理I/O事件
		 */
		cause.printStackTrace();
		ctx.close();
	}
}
