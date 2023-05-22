package com.jiquan.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author ZHONG Jiquan
 * @year 2023
 */
public class DateUtil {

	public static Date get(String pattern){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			return sdf.parse(pattern);
		}catch(ParseException e){
			throw new RuntimeException(e);
		}
	}
}
