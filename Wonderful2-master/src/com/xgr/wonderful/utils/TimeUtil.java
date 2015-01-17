package com.xgr.wonderful.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.text.TextUtils;

public class TimeUtil {
	/** 格式定义 yyyy-MM-dd */
	public static String format = "yyyy-MM-dd";
	/** 格式定义 yyyy-MM-dd HH:mm:ss */
	public static String format1 = "yyyy-MM-dd HH:mm:ss";
	/** 格式定义 yyyyMMddHHmmss */
	public static String format2 = "yyyyMMdd";
	/** 格式定义 yyyyMMddHHmmss */
	public static String format3 = "yyyy-MM-ddHH:mm:ss";
	public static String format5 = "yyyyMMddHHmmss";
    

	/**
	 * 获取驾驶时间（时、分、秒）
	 * param startTime, endTime
	 * @return int[]{h, m, s}
	 */
	private static int[] getDrivingDurationForIntArray(long startTime, long endTime) {
		if(endTime == 0) {
			endTime = System.currentTimeMillis();
		} 
		int second = (int) ((endTime - startTime) / 1000);
		int minute = 0;
		int hour = 0;
		if (second > 60) {            
			minute = second / 60;            
			second = second % 60;        
		}       
		if (minute > 60) {            
			hour = minute / 60;            
			minute = minute % 60;        
		}    
		int[] intArray = new int[]{hour, minute, second};
		return intArray;
	}
	/**
	 * 将毫秒数转换为hh小时mm分格式 ,分钟四舍五入
	 * @param time
	 * @return
	 */
	public static String getHoueAndMinute(long time){
		if(time == 0) {
			return 0+"min";
		}
		String str = "";
		int second = (int) (time / (1000)%60);
		int minute = (int)(time/(1000*60));
		int hour = 0;
		if(minute>=60){
			hour = minute/60;
			minute = minute%60;
		}
		minute += (int)Math.rint(((float)second)/60);
		if(hour != 0) 
			str = hour + "h";
		if(minute != 0)
			str += minute + "min";
		return str;
	}
	public static double getDrivingDurationFormatHours(long startTime, long endTime) {
		if(endTime == 0) {
			endTime = System.currentTimeMillis();
		} 
		int[] intArray = getDrivingDurationForIntArray(startTime, endTime);
		return  intArray[0] + (double)intArray[1]/60 + (double)intArray[2]/3600; 
	}

	/**
	 * 获取当前的驾驶时长（在驾驶中调用）
	 * @param startTime
	 * @return
	 */
	public static String getCurrentDrivingDuration(long startTime) {
		// TODO Auto-generated method stub
		return getCurrentDrivingDuration(startTime, 0);
	}
	
	private static String getCurrentDrivingDuration(long startTime, long endTime) {
		String result = "";
		int[] intArray = getDrivingDurationForIntArray(startTime, endTime);
		if(null == intArray) {
			return result;
		}
		if(intArray[0] < 10) {
			result += ( "0" + intArray[0] + ":");
		} else {
			result += intArray[0] + ":";
		}
		if(intArray[1] < 10) {
			result += ("0" + intArray[1] + ":");
		} else {
			result += intArray[1] + ":";
		}
		if(intArray[2] < 10) {
			result +=("0" +  intArray[2]);
		} else {
			result += intArray[2];
		}
		return result;
	}


	/**
	 * 获取驾驶时长（查看历史数据时使用）
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static String getDrivingDuration(String startTime, String endTime) {
		// TODO Auto-generated method stub
		if(TextUtils.isEmpty(startTime) || TextUtils.isEmpty(endTime)) {
			return "";
		}
		String result = "";
		SimpleDateFormat sdf = new SimpleDateFormat(format1);
		try {
			long start = sdf.parse(startTime).getTime();
			long end = sdf.parse(endTime).getTime();
			result = getCurrentDrivingDuration(start, end);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("getDrivingDuration------时间转换错误");
		}
		return result;
	}
   public static long stringTolong(String time){
	   if(TextUtils.isEmpty(time))
		   return 0;
	   long t = 0;
	   SimpleDateFormat sdf = new SimpleDateFormat(format1);
	   try {
		t = sdf.parse(time).getTime();
	} catch (ParseException e) {
		// TODO: handle exception
	}
	   return t;
   }
   
   public static String getHHmm(String time) {
	   String result = "";
	   if(TextUtils.isEmpty(time))
		   return result;
	   long ltime = stringTolong(time);
	   if(ltime == 0)
		   return result;
	   Calendar calendar = Calendar.getInstance();
	   calendar.setTimeInMillis(ltime);
	   int hour = calendar.get(Calendar.HOUR_OF_DAY);
	   int mm = calendar.get(Calendar.MINUTE);
	   if(mm < 10) {
		   result += hour + ":0" + mm;
	   } else{
		   result += hour + ":" + mm;
	   }
	   return result;
   }
   
	/**
	 * 时间转换类
	 * @param dateStr, formatStr
	 * @return
	 */
	public static String dataFormatMMdd(String dateStr, String formatStr) {
		// TODO Auto-generated method stub
		if(TextUtils.isEmpty(dateStr)) {
			return "";
		}
		String result = "";
		SimpleDateFormat sdf;
		if(TextUtils.isEmpty(formatStr)) {
			sdf = new SimpleDateFormat(format1);
		} else {
			sdf = new SimpleDateFormat(formatStr);
		}
		try {
			Date date = sdf.parse(dateStr);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			String month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
			if(1 == month.length()) {
				month = "0" + month;
			}
			String day = String.valueOf(calendar.get(Calendar.DATE));
			if(1 == day.length()) {
				day = "0" + day;
			}
			result +=month + "月" + day + "日";
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("dataFormatMMdd--------时间转换错误");
		}
		return result;
	}

	/**
	 * 将long类型的时间转换为string
	 * @param time, formatStr
	 * @return
	 */
	public static String dateLongFormatString(long time, String formatStr) {
		// TODO Auto-generated method stub
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		SimpleDateFormat sdf;
		if(TextUtils.isEmpty(formatStr)) {
			sdf = new SimpleDateFormat(format1, Locale.getDefault());
		} else {
			sdf = new SimpleDateFormat(formatStr, Locale.getDefault());
		}
		return sdf.format(calendar.getTime());
	}
	/**
	 * 将"yyyy-MM-dd HH:mm:ss"格式的字符串转为UTC时间的"yyyy-MM-dd HH:mm:ss"格式的字符串
	 * 
	 * @param time
	 * @return
	 */
	public static String parseToUTC(String time) {
		if(TextUtils.isEmpty(time))
			return "";
		String newTime = new String();
		try {
			SimpleDateFormat sdfUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
			SimpleDateFormat sdfLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
			sdfUTC.setTimeZone(TimeZone.getTimeZone("UTC"));// 设置
			newTime = sdfUTC.format(sdfLocal.parse(time));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return newTime;
	}
	public static String parseToLocal(String time) {
		String newTime = "";
		SimpleDateFormat sdfUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
		SimpleDateFormat sdfLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
		sdfUTC.setTimeZone(TimeZone.getTimeZone("UTC"));// 设置
		sdfLocal.setTimeZone(TimeZone.getDefault());
		Date date = null;
		try {
			date = sdfUTC.parse(time);
			newTime = sdfLocal.format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newTime;
	}
}
