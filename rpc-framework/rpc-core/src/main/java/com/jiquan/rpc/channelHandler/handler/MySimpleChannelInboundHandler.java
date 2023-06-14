package com.jiquan.rpc.channelHandler.handler;

import com.jiquan.rpc.RpcBootstrap;
import com.jiquan.rpc.transport.message.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<RpcResponse> {
	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
		Object returnValue = rpcResponse.getBody();
		CompletableFuture<Object> completableFuture = RpcBootstrap.PENDING_REQUEST.get(1L);
		completableFuture.complete(returnValue);
		if(log.isDebugEnabled()){
			log.debug("find the completableFuture of request {}",rpcResponse.getRequestId());
		}
	}
}
