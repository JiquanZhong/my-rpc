package com.jiquan.exceptions;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
public class SerializeException extends RuntimeException{
	public SerializeException() {
	}

	public SerializeException(String message) {
		super(message);
	}

	public SerializeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SerializeException(Throwable cause) {
		super(cause);
	}

	public SerializeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
