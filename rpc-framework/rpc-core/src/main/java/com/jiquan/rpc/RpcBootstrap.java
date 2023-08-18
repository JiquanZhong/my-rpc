package com.jiquan.rpc;

import com.jiquan.IdGenerator;
import com.jiquan.rpc.annotation.RpcAPI;
import com.jiquan.rpc.channelhandler.handler.MethodCallHandler;
import com.jiquan.rpc.channelhandler.handler.RpcRequestDecoder;
import com.jiquan.rpc.channelhandler.handler.RpcResponseEncoder;
import com.jiquan.rpc.core.HeartbeatDetector;
import com.jiquan.rpc.discovery.Registry;
import com.jiquan.rpc.discovery.RegistryConfig;
import com.jiquan.rpc.loadbalance.LoadBalancer;
import com.jiquan.rpc.loadbalance.impl.MinimumResponseTimeLoadBalancer;
import com.jiquan.rpc.loadbalance.impl.RoundRobinLoadBalancer;
import com.jiquan.rpc.transport.message.RpcRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class RpcBootstrap {
	// singleton
	private static final RpcBootstrap rpcBootStrap = new RpcBootstrap();
	private String appName = "default name";
	private RegistryConfig registryConfig;
	private ProtocolConfig protocolConfig;
	public static final int PORT = 8090;
	public static final IdGenerator ID_GENERATOR = new IdGenerator(1, 2);
	public static String COMPRESS_TYPE = "gzip";
	public static String SERIALIZE_TYPE = "hessian";
	public static LoadBalancer LOAD_BALANCE;
	public static final ThreadLocal<RpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();

	private Registry registry;

	// Connection cache, if you use a class like InetSocketAddress as the key, be sure to see if he has rewritten the equals method and toString method
	public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);
	// Maintain published and exposed service list key-> fully qualified name of interface value -> ServiceConfig
	public static final Map<String, ServiceConfig<?>> SERVICE_LIST = new ConcurrentHashMap<>(16);
	// Define a global external pending completableFuture
	public static final Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(128);
	public final static TreeMap<Long, Channel> ANSWER_TIME_CACHE = new TreeMap<>();


	private RpcBootstrap() {
		// TODO initialization
	}

	public static RpcBootstrap getInstance() {
		return rpcBootStrap;
	}

	/**
	 * @param appName
	 * @return
	 */
	public RpcBootstrap application(String appName) {
		this.appName = appName;
		return this;
	}

	/**
	 * Used to configure a registry
	 *
	 * @param registryConfig registration center
	 * @return this current instance
	 */
	public RpcBootstrap registry(RegistryConfig registryConfig) {
		// A zookeeper instance is maintained here, but if written in this way, zookeeper will be coupled with the current project
		// We actually hope that we can expand more different implementations in the future
		// Try to use registryConfig to get a registration center, which is a bit of a factory design pattern
		this.registry = registryConfig.getRegistry();
		// the first time when the client tries to registry with registryConfig, RpcBootstrap will create a global instance LoadBalancer
//		LOAD_BALANCE = new ConsistentHashLoadBalancer();
//		LOAD_BALANCE = new MinimumResponseTimeLoadBalancer();
		LOAD_BALANCE = new RoundRobinLoadBalancer();
		return this;
	}

	/**
	 * Configure the protocol used by the currently exposed service
	 *
	 * @param protocolConfig protocol encapsulation
	 * @return this current instance
	 */
	public RpcBootstrap protocol(ProtocolConfig protocolConfig) {
		this.protocolConfig = protocolConfig;
		if(log.isDebugEnabled()) log.debug("Current app is using {} for serialization", protocolConfig.toString());
		return this;
	}


	/**
	 * Publish the service, implement the interface and register it with the service center
	 *
	 * @param service encapsulated service that needs to be published
	 * @return this current instance
	 */
	public RpcBootstrap publish(ServiceConfig service) {
		registry.register(service);
		SERVICE_LIST.put(service.getInterface().getName(), service);
		return this;
	}

	public RpcBootstrap publish(List<ServiceConfig> services) {
		for(ServiceConfig<?> service : services) {
			this.publish(service);
		}
		return this;
	}

	public RpcBootstrap reference(ReferenceConfig<?> reference) {
		// In this method, can we get the relevant configuration items - registration center
		// Configure the reference to facilitate generation of proxy objects when the get method is called in the future
		// 1. The reference needs a registration center
		HeartbeatDetector.detectHeartbeat(reference.getInterfaceRef().getName());
		reference.setRegistry(registry);
		return this;
	}

	public Registry getRegistry() {
		return registry;
	}

	/**
	 * start the netty server
	 */
	public void start() {
		// 1. create a eventLoop, boss distribute the request to the worker
		EventLoopGroup boss = new NioEventLoopGroup(2);
		EventLoopGroup worker = new NioEventLoopGroup(10);

		try {
			// 2. create a serverBoostrap and
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			// 3. configure it with Handlers
			serverBootstrap.group(boss, worker)
					.channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel socketChannel) throws Exception {
							socketChannel.pipeline()
									.addLast(new LoggingHandler())
									.addLast(new RpcRequestDecoder())
									.addLast(new MethodCallHandler())
									.addLast(new RpcResponseEncoder());

						}
					});

			// 4. Bind the serverBoostrap configuration to the port
			ChannelFuture channelFuture = serverBootstrap.bind(PORT).sync();

			channelFuture.channel().closeFuture().sync();
		} catch(InterruptedException e) {
			e.printStackTrace();
		} finally {
			try {
				boss.shutdownGracefully().sync();
				worker.shutdownGracefully().sync();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public RpcBootstrap scan(String packageName) {
		List<String> classNames = getAllClassNames(packageName);
		List<Class<?>> classes = classNames.stream()
				.map(className -> {
					try {
						return Class.forName(className);
					} catch (ClassNotFoundException e) {
						throw new RuntimeException(e);
					}
				}).filter(clazz -> clazz.getAnnotation(RpcAPI.class) != null)
				.collect(Collectors.toList());

		for (Class<?> clazz : classes) {
			Class<?>[] interfaces = clazz.getInterfaces();
			Object instance = null;
			try {
				instance = clazz.getConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException |
					 NoSuchMethodException e) {
				throw new RuntimeException(e);
			}

			List<ServiceConfig<?>> serviceConfigs = new ArrayList<>();
			for (Class<?> anInterface : interfaces) {
				ServiceConfig<?> serviceConfig = new ServiceConfig<>();
				serviceConfig.setInterface(anInterface);
				serviceConfig.setRef(instance);
				if (log.isDebugEnabled()){
					log.debug("scanning the package and publish service {}",anInterface);
				}
				publish(serviceConfig);
			}

		}
		return this;
	}

	private List<String> getAllClassNames(String packageName) {
		// com.xxx.yyy -> E://xxx/xww/sss/com/xxx/yyy
		String basePath = packageName.replaceAll("\\.", "/");
		URL url = ClassLoader.getSystemClassLoader().getResource(basePath);
		if(url == null) {
			throw new RuntimeException("Cannot find the path");
		}
		String absolutePath = url.getPath();
		return recursionFile(absolutePath, new ArrayList<>(), basePath);
	}

	private List<String> recursionFile(String absolutePath, List<String> classNames, String basePath) {
		// 获取文件
		File file = new File(absolutePath);
		// 判断文件是否是文件夹
		if(file.isDirectory()) {
			// 找到文件夹的所有的文件
			File[] children = file.listFiles(pathname -> pathname.isDirectory() || pathname.getPath().contains(".class"));
			if(children == null || children.length == 0) {
				return classNames;
			}
			for(File child : children) {
				if(child.isDirectory()) {
					recursionFile(child.getAbsolutePath(), classNames, basePath);
				} else {
					// 文件 --> 类的权限定名称
					String className = getClassNameByAbsolutePath(child.getAbsolutePath(), basePath);
					classNames.add(className);
				}
			}

		} else {
			// 文件 --> 类的权限定名称
			String className = getClassNameByAbsolutePath(absolutePath, basePath);
			classNames.add(className);
		}
		return classNames;
	}

	private String getClassNameByAbsolutePath(String absolutePath, String basePath) {
		String os = System.getProperty("os.name").toLowerCase();
		String fileName;
		if(os.contains("win")){
			fileName = absolutePath
					.substring(absolutePath.indexOf(basePath.replaceAll("/","\\\\")))
					.replaceAll("\\\\",".");
		}else{
			fileName = absolutePath.substring(absolutePath.indexOf(basePath)).replaceAll("/", ".");
		}

		fileName = fileName.substring(0,fileName.indexOf(".class"));
		return fileName;
	}

	public static void main(String[] args) {
		List<String> allClassNames = RpcBootstrap.getInstance().getAllClassNames("com.jiquan.rpc.compress");
		System.out.println(allClassNames);
	}
}
