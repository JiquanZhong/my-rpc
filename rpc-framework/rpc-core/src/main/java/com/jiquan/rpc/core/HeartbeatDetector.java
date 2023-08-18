package com.jiquan.rpc.core;

import com.jiquan.rpc.NettyBootstrapInitializer;
import com.jiquan.rpc.RpcBootstrap;
import com.jiquan.rpc.compress.CompressorFactory;
import com.jiquan.rpc.enumeration.RequestType;
import com.jiquan.rpc.serialize.SerializerFactory;
import com.jiquan.rpc.transport.message.RpcRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class HeartbeatDetector {
	public static void detectHeartbeat(String serviceName) {
		List<InetSocketAddress> addresses = RpcBootstrap.getInstance().getRegistry().lookup(serviceName);

		for(InetSocketAddress address : addresses) {
			try {
				if(!RpcBootstrap.CHANNEL_CACHE.containsKey(address)) {
					Channel channel = NettyBootstrapInitializer.getBootstrap().connect(address).sync().channel();
					RpcBootstrap.CHANNEL_CACHE.put(address, channel);
				}
			} catch(InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		Thread thread = new Thread(() -> {
			new Timer().scheduleAtFixedRate(new MyTimerTask(), 0, 3000);
		}, "rpc-heartbeatDetector-thread");
		thread.setDaemon(true);
		thread.start();
	}

	private static class MyTimerTask extends TimerTask {
		@Override
		public void run() {
			RpcBootstrap.ANSWER_TIME_CACHE.clear();

			Map<InetSocketAddress, Channel> cache = RpcBootstrap.CHANNEL_CACHE;
			for(Map.Entry<InetSocketAddress, Channel> entry : cache.entrySet()) {
				int tryTimes = 3;
				while (tryTimes > 0){
					Channel channel = entry.getValue();
					long startTime = System.currentTimeMillis();

					RpcRequest rpcRequest = RpcRequest.builder()
							.requestId(RpcBootstrap.ID_GENERATOR.getId())
							.compressType(CompressorFactory.getCompressor(RpcBootstrap.COMPRESS_TYPE).getCode())
							.requestType(RequestType.HEARTBEAT.getId())
							.serializeType(SerializerFactory.getSerializer(RpcBootstrap.SERIALIZE_TYPE).getCode())
							.timeStamp(startTime)
							.build();

					CompletableFuture<Object> completableFuture = new CompletableFuture<>();
					RpcBootstrap.PENDING_REQUEST.put(rpcRequest.getRequestId(), completableFuture);

					channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) promise -> {
						if(!promise.isSuccess()) {
							completableFuture.completeExceptionally(promise.cause());
						}
					});

					Long endTime = 0L;
					try {
						completableFuture.get(1, TimeUnit.SECONDS);
						endTime = System.currentTimeMillis();
					} catch(InterruptedException | ExecutionException | TimeoutException e) {
						tryTimes--;
						log.error("error of connection with {}, trying to connect the {} times", entry.getKey(), 3-tryTimes);
						if(tryTimes == 0) RpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());

						try {
							Thread.sleep(new Random().nextInt(50));
						} catch(InterruptedException ex) {
							throw new RuntimeException(ex);
						}
						continue;
					}
					Long time = endTime - startTime;
					RpcBootstrap.ANSWER_TIME_CACHE.put(time, channel);
					log.debug("HEARTBEAT: the delay time with {} is {} ms", entry.getKey(), time);
					break;
				}
			}
		}
	}

}
