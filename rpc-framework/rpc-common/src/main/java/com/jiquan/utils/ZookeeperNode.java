package com.jiquan.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZookeeperNode {
	private String nodePath;
	private byte[] data;

}
