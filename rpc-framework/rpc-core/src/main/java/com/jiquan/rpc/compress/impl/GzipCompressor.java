package com.jiquan.rpc.compress.impl;

import com.jiquan.exceptions.CompressException;
import com.jiquan.rpc.compress.Compressor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@Slf4j
public class GzipCompressor implements Compressor {
	@Override
	public byte[] compress(byte[] bytes) {
		try (
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos)
		) {
			gzipOutputStream.write(bytes);
			gzipOutputStream.finish();
			byte[] result = baos.toByteArray();
			if(log.isDebugEnabled()) log.debug("compress the byte[] from {} to {}", bytes.length, result.length);
			return result;
		}catch(IOException e){
			log.error("error when compressing the byte array",e);
			throw new CompressException();
		}
	}

	@Override
	public byte[] decompress(byte[] bytes) {
		try (
				ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
				GZIPInputStream gzipInputStream = new GZIPInputStream(bais)
		) {
			byte[] result = gzipInputStream.readAllBytes();
			if(log.isDebugEnabled()) log.debug("decompress the byte[] from {} to {}", bytes.length, result.length);
			return result;
		}catch(IOException e){
			log.error("error when decompressing the byte array",e);
			throw new CompressException();
		}
	}
}
