package com.jiquan.rpc.compress;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CompressWrapper {
	private byte code;
	private String type;
	private Compressor compressor;
}
