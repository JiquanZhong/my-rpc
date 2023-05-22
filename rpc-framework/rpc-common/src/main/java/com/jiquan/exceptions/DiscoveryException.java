package com.jiquan.exceptions;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
public class DiscoveryException extends RuntimeException{
	public DiscoveryException() {
	}

	public DiscoveryException(String message) {
		super(message);
	}

	public DiscoveryException(Throwable cause) {
		super(cause);
	}
}
