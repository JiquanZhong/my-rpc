package com.jiquan.rpc.proxy.handler;

import com.jiquan.exceptions.DiscoveryException;
import com.jiquan.exceptions.NetworkException;
import com.jiquan.rpc.NettyBootstrapInitializer;
import com.jiquan.rpc.RpcBootstrap;
import com.jiquan.rpc.compress.CompressorFactory;
import com.jiquan.rpc.discovery.Registry;
import com.jiquan.rpc.enumeration.RequestType;
import com.jiquan.rpc.serialize.SerializerFactory;
import com.jiquan.rpc.transport.message.RequestPayload;
import com.jiquan.rpc.transport.message.RpcRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class RpcConsumerInvocationHandler implements InvocationHandler {
	private final Registry registry;
	private final Class<?> interfaceRef;

	public RpcConsumerInvocationHandler(Registry registry, Class<?> interfaceRef) {
		this.registry = registry;
		this.interfaceRef = interfaceRef;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		/*
		 * ------------------ Encapsulated message ---------------------------
		 */
		RequestPayload requestPayload = RequestPayload.builder()
				.interfaceName(interfaceRef.getName())
				.methodName(method.getName())
				.parametersType(method.getParameterTypes())
				.parametersValue(args)
				.returnType(method.getReturnType())
				.build();

		// todo needs to process the request id and various types
		RpcRequest rpcRequest = RpcRequest.builder()
				.requestId(RpcBootstrap.getInstance().getConfiguration().getIdGenerator().getId())
				.compressType(CompressorFactory.getCompressor(RpcBootstrap.getInstance().getConfiguration().getCompressType()).getCode())
				.requestType(RequestType.REQUEST.getId())
				.serializeType(SerializerFactory.getSerializer(RpcBootstrap.getInstance().getConfiguration().getSerializeType()).getCode())
				.timeStamp(new Date().getTime())
				.requestPayload(requestPayload)
				.build();

		RpcBootstrap.REQUEST_THREAD_LOCAL.set(rpcRequest);

		// 1. Discovery services, from the registry, look for an available service
		//		InetSocketAddress address = registry.lookup(interfaceRef.getName());
		// implementation of load balance
		InetSocketAddress address = RpcBootstrap.getInstance().getConfiguration().getLoadBalancer().selectServiceAddress(interfaceRef.getName());
		// should remove the request ThreadLocal
		RpcBootstrap.REQUEST_THREAD_LOCAL.remove();

		if (log.isDebugEnabled()) {
			log.debug("consumer found an available service {} on {}.",
					  interfaceRef.getName(), address);
		}

		// 2. Try to get an available channel
		Channel channel = getAvailableChannel(address);
		if (log.isDebugEnabled()) {
			log.debug("Obtained the connection channel established with {}, ready to send data.", address);
		}

		RpcBootstrap.getInstance().getConfiguration().setSerializeType(SerializerFactory.CODE_STRING_MAP.get(rpcRequest.getSerializeType()));

		/*
		 * ------------------synchronization-------------------------
		 */
//                ChannelFuture channelFuture = channel.writeAndFlush(new Object()).await();
//                if(channelFuture.isDone()){
//                    Object object = channelFuture.getNow();
//                } else if( !channelFuture.isSuccess() ){
//                    // 需要捕获异常,可以捕获异步任务中的异常
//                    Throwable cause = channelFuture.cause();
//                    throw new RuntimeException(cause);
//                }

		// 4. Write out the message
		CompletableFuture<Object> completableFuture = new CompletableFuture<>();
		// expose completableFuture
		RpcBootstrap.PENDING_REQUEST.put(rpcRequest.getRequestId(), completableFuture);
		channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) promise -> {
			if (!promise.isSuccess()) {
				completableFuture.completeExceptionally(promise.cause());
			}
		});

		// 5. Get the result of the response
		return completableFuture.get(10, TimeUnit.SECONDS);
	}

	/**
	 * Getting an available channel with the specific address
	 * @param address
	 * @return
	 */
	private Channel getAvailableChannel(InetSocketAddress address) {
		// 1. try to get one from the cache
		Channel channel = RpcBootstrap.CHANNEL_CACHE.get(address);
		// 2. create one if it doesn't exist
		if(channel == null) {
			// blocking action
//                    channel = NettyBootstrapInitializer.getBootstrap()
//                        .connect(address).await().channel();
			CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
			NettyBootstrapInitializer.getBootstrap().connect(address).addListener((ChannelFutureListener) promise -> {
				if(promise.isDone()) {
					if(log.isDebugEnabled())
						log.debug("succeed establishing the connection with {}", address.getAddress());
					channelFuture.complete(promise.channel());
				} else if(!promise.isSuccess()) {
					channelFuture.completeExceptionally(promise.cause());
				}
			});

			// 3. blocking action: wait to get the channel
			try {
				channel = channelFuture.get(3, TimeUnit.SECONDS);
			} catch(InterruptedException | ExecutionException | TimeoutException e) {
				log.error("error when getting the channel", e);
				throw new DiscoveryException(e);
			}

			// 4. put the channel to the cache and return it
			RpcBootstrap.CHANNEL_CACHE.put(address, channel);
		}
		if(channel == null) {
			log.error("error when trying to connecting with {}", address.getAddress());
			throw new NetworkException("Error when getting the channel");
		}
		return channel;
	}
}
