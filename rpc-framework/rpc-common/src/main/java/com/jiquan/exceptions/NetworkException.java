package com.jiquan.exceptions;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
public class NetworkException extends RuntimeException{
	public NetworkException() {
		super();
	}

	public NetworkException(String message) {
		super(message);
	}

	public NetworkException(Throwable cause) {
		super(cause);
	}
}
