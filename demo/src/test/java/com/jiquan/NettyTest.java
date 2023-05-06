package com.jiquan;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author ZHONG Jiquan
 * @create 06/08/2023 - 01:00
 */
public class NettyTest {
	@Test
	public void testCompositeByteBuf() {
		ByteBuf header = Unpooled.buffer();
		ByteBuf body = Unpooled.buffer();

		// 0 copy
		// logical composition instead of physical composition
		CompositeByteBuf byteBuf = Unpooled.compositeBuffer();
		byteBuf.addComponents(header, body);

	}

	@Test
	public void testWrapper() {
		byte[] buf = new byte[1024];
		byte[] buf2 = new byte[1024];
		// share the content of byte[] which is equal to the Zero Copy
		ByteBuf byteBuf = Unpooled.wrappedBuffer(buf, buf2);
	}

	@Test
	public void testSlice() {
		byte[] buf = new byte[1024];
		byte[] buf2 = new byte[1024];
		// share the content of byte[] which is equal to the Zero Copy
		ByteBuf byteBuf = Unpooled.wrappedBuffer(buf, buf2);

		ByteBuf buf1 = byteBuf.slice(0,5);
		ByteBuf buf3 = byteBuf.slice(5,10);
	}

	@Test
	public void testMessage() throws IOException {
		ByteBuf message = Unpooled.buffer();
		message.writeBytes("head".getBytes(StandardCharsets.UTF_8));
		message.writeByte(1);
		message.writeShort(125);
		message.writeInt(256);
	}

	@Test
	public void testCompress() throws IOException {
		byte[] buf = new byte[]{12,13,56,34,2,2,43,5,3,2,1,4,34,5,53,23,34,35,54,2,2,5,5,3,3,2,2,2,44,5,5,35,2,43,5,3,2,1,4,34,2,43,5,3,2,1,4,34,2,43,5,3,2,1,4,34,2,43,5,3,2,1,4,34,2,43,5,3,2,1,4,34,2,43,5,3,2,1,4,34,2,43,5,3,2,1,4,34,2,43,5,3,2,1,4,34,2,43,5,3,2,1,4,34,2,43,5,3,2,1,4,34,2,43,5,3,2,1,4,34,2,43,5,3,2,1,4,34,2,43,5,3,2,1,4,34,2,43,5,3,2,1,4,34,2,43,5,3,2,1,4,34,2,43,5,3,2,1,4,34,2,43,5,3,2,1,4,34,2,43,5,3,2,1,4,34,2,43,5,3,2,1,4,34,2,43,5,3,2,1,4,34,2,43,5,3,2,1,4,34,2,43,5,3,2,1,4,34,2,43,5,3,2,1,4,34,2,43,5,3,2,1,4,34};

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPOutputStream gos = new GZIPOutputStream(baos);
		gos.write(buf);
		gos.finish();

		byte[] compressBytes = baos.toByteArray();
		System.out.println("大小: " + buf.length + " -> " + compressBytes.length);
		System.out.println("压缩后"+Arrays.toString(compressBytes));

		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressBytes);
		GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
		byte[] bytes = gzipInputStream.readAllBytes();
		System.out.println("解压后"+Arrays.toString(bytes));
		System.out.println(Arrays.equals(buf,bytes));
	}
}
