package com.jiquan.demo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import java.nio.charset.StandardCharsets;

/**
 * @author ZHONG Jiquan
 * @create 06/08/2023 - 01:32
 */
// 为了线程安全
@ChannelHandler.Sharable
public class MyChannelHandler2 extends ChannelInboundHandlerAdapter {
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf byteBuf = (ByteBuf) msg;
		System.out.println("收到服务器的回复："+byteBuf.toString(StandardCharsets.UTF_8));
	}
}
