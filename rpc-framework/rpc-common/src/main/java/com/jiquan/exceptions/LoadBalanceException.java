package com.jiquan.exceptions;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
public class LoadBalanceException extends RuntimeException{
	public LoadBalanceException() {
	}

	public LoadBalanceException(String message) {
		super(message);
	}

	public LoadBalanceException(String message, Throwable cause) {
		super(message, cause);
	}

	public LoadBalanceException(Throwable cause) {
		super(cause);
	}

	public LoadBalanceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
