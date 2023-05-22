package com.jiquan;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
public class Constant {
	// default zk connection
	public static final String DEFAULT_ZK_CONNECTION = "127.0.0.1:2181";

	// default zk timeout
	public static final int TIME_OUT = 10000;

	// the default nodes of service for the consumer and provider
	public static final String BASE_PROVIDER_PATH = "/rpc-metadata/providers";
	public static final String BASE_CONSUMER_PATH = "/rpc-metadata/consumers";
}
