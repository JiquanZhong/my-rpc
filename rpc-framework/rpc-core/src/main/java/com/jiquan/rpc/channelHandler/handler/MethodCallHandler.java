package com.jiquan.rpc.channelHandler.handler;

import com.jiquan.rpc.RpcBootstrap;
import com.jiquan.rpc.ServiceConfig;
import com.jiquan.rpc.transport.message.RequestPayload;
import com.jiquan.rpc.transport.message.RpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<RpcRequest> {
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
		// 1. get the request payload
		RequestPayload requestPayload = rpcRequest.getRequestPayload();
		// 2. invoke method according to the content of payload
		Object o = callTargetMethod(requestPayload);

		// todo encapsulate the response

		// write the response
		ctx.channel().writeAndFlush(o);
	}

	private Object callTargetMethod(RequestPayload requestPayload){
		String interfaceName = requestPayload.getInterfaceName();
		String methodName = requestPayload.getMethodName();
		Class<?>[] parametersType = requestPayload.getParametersType();
		Object[] parametersValue = requestPayload.getParametersValue();

		ServiceConfig<?> serviceConfig = RpcBootstrap.SERVICE_LIST.get(interfaceName);
		Object refImpl = serviceConfig.getRef();

		Object returnValue;
		try {
			Class<?> aClass = refImpl.getClass();
			Method method = aClass.getMethod(methodName, parametersType);
			returnValue = method.invoke(refImpl, parametersValue);

		}catch(InvocationTargetException | NoSuchMethodException | IllegalAccessException e){
			log.error("error when invoking the method {} of {}", methodName, interfaceName, e);
			throw new RuntimeException(e);
		}
		return returnValue;
	}
}
