package com.jiquan.rpc.compress;

import com.jiquan.rpc.compress.impl.GzipCompressor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class CompressorFactory {
	private static final ConcurrentHashMap<String, CompressWrapper> COMPRESSOR_CACHE = new ConcurrentHashMap<>(8);
	private static final ConcurrentHashMap<Byte, CompressWrapper> COMPRESSOR_CODE_CACHE = new ConcurrentHashMap<>(8);

	static {
		CompressWrapper gzip = new CompressWrapper((byte) 1, "gzip", new GzipCompressor());
		COMPRESSOR_CACHE.put(gzip.getType(), gzip);
		COMPRESSOR_CODE_CACHE.put(gzip.getCode(), gzip);
	}

	public static CompressWrapper getCompressor(byte code){
		CompressWrapper compressWrapper = COMPRESSOR_CODE_CACHE.get(code);
		if(compressWrapper == null){
			if(log.isDebugEnabled()) log.debug("cannot find the compressor with code {}, using the default gzip compressor", code);
			return COMPRESSOR_CODE_CACHE.get((byte) 1);
		}
		return compressWrapper;
	}

	public static CompressWrapper getCompressor(String type){
		CompressWrapper compressWrapper = COMPRESSOR_CACHE.get(type);
		if(compressWrapper == null){
			if(log.isDebugEnabled()) log.debug("cannot find the compressor with type {}, using the default gzip compressor", type);
			return COMPRESSOR_CACHE.get("gzip");
		}
		return compressWrapper;
	}
}
