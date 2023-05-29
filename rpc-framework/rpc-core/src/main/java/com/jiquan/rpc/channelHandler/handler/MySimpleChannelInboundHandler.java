package com.jiquan.rpc.channelHandler.handler;

import com.jiquan.rpc.RpcBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {
	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf msg) throws Exception {
		String result = msg.toString(Charset.defaultCharset());
		CompletableFuture<Object> completableFuture = RpcBootstrap.PENDING_REQUEST.get(1L);
		completableFuture.complete(result);
	}
}
