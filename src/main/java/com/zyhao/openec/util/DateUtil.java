package com.zyhao.openec.util;

import java.util.Date;
import org.apache.commons.lang.time.DateFormatUtils;

public class DateUtil {
	public static String getDefaultDate() {
		String format = "yyyy-MM-dd HH:mm:ss";
		Date calendar = new Date();
		return DateFormatUtils.format(calendar , format);
	}
	
}
