package com.jiquan.rpc.compress;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
public interface Compressor {
	/**
	 * compress the input byte array
	 * @param bytes
	 * @return
	 */
	byte[] compress(byte[] bytes);

	/**
	 * decompress the input byte array
	 * @param bytes
	 * @return
	 */
	byte[] decompress(byte[] bytes);
}
