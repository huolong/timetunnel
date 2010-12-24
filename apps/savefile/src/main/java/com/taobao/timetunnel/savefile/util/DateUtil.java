package com.taobao.timetunnel.savefile.util;
import java.util.*;
public class DateUtil {
	public static void main(String args[]) throws Exception{
		Date d=new Date();
		Date d1=DateUtil.beginOfDate(d);
		System.out.println("l "+d1);
		Thread.sleep(3000);
		 d=new Date();
		 d1=DateUtil.beginOfDate(d);
		System.out.println("l "+d1);
		for (int i=0;i<25;i++) {
		 d1=nextHour(d1);
		  System.out.println("ll "+d1);
		}
		}
	public static Date parseDate(String separator,String s) {
		String[] tokens=s.split(separator);
		Calendar cal=Calendar.getInstance();
		if (tokens.length==4)
	 	   cal.set(Integer.valueOf(tokens[0]), Integer.valueOf(tokens[1])-1,Integer.valueOf(tokens[2]),
				Integer.valueOf(tokens[3]),0,0);
		else
			cal.set(Integer.valueOf(tokens[0]), Integer.valueOf(tokens[1])-1,Integer.valueOf(tokens[2]),
					Integer.valueOf(tokens[3]),Integer.valueOf(tokens[4]),0);
		return cal.getTime();
		
	}
	public static String preDate(char separator){
		Calendar cal=Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		StringBuilder sb=new StringBuilder();
		sb.append(cal.get(Calendar.YEAR));
		sb.append(separator);
		sb.append(padding(cal.get(Calendar.MONTH)+1));
		sb.append(separator);
		sb.append(padding(cal.get(Calendar.DAY_OF_MONTH)));
		return sb.toString();
	}
	public static String getTimeStampInHour(char separator,Date date) {
		Calendar cal=Calendar.getInstance();
		if (date!=null)
		  cal.setTime(date);
		StringBuilder sb=new StringBuilder();
		sb.append(cal.get(Calendar.YEAR));
//		sb.append(separator);
		sb.append(padding(cal.get(Calendar.MONTH)+1));
//		sb.append(separator);
		sb.append(padding(cal.get(Calendar.DAY_OF_MONTH)));
		sb.append(separator);
		sb.append(padding(cal.get(Calendar.HOUR_OF_DAY)));
		return sb.toString();
	}
	public static String getTimeStampInMin(char separator,Date date) {
		Calendar cal=Calendar.getInstance();
		if (date!=null)
		  cal.setTime(date);
		StringBuilder sb=new StringBuilder();
		sb.append(cal.get(Calendar.YEAR));
		sb.append(separator);
		sb.append(padding(cal.get(Calendar.MONTH)+1));
		sb.append(separator);
		sb.append(padding(cal.get(Calendar.DAY_OF_MONTH)));
		sb.append(separator);
		sb.append(padding(cal.get(Calendar.HOUR_OF_DAY)));
		sb.append(separator);
		sb.append(padding(cal.get(Calendar.MINUTE)));
		return sb.toString();
	}

	public static String getTimeStampInMin(Date date) {
		Calendar cal=Calendar.getInstance();
		if (date!=null)
		  cal.setTime(date);
		StringBuilder sb=new StringBuilder();
		sb.append(cal.get(Calendar.YEAR));
		sb.append(padding(cal.get(Calendar.MONTH)+1));
		sb.append(padding(cal.get(Calendar.DAY_OF_MONTH)));
		sb.append("/");
		sb.append(padding(cal.get(Calendar.HOUR_OF_DAY)));
		sb.append("/");
		sb.append(padding(cal.get(Calendar.MINUTE)));
		return sb.toString();
	}
	
	public static String padding(int i) {
		if (i==0)
			return "00";
		else
		if (i<10)
			return "0"+i;
		else
			return ""+i;
   }
   public static Date beginOfDate(Date date) {
	   Calendar cal=Calendar.getInstance();
	   cal.setTime(date);
	   cal.set(Calendar.HOUR_OF_DAY, 0);
	   cal.set(Calendar.MINUTE,0);
	   cal.set(Calendar.SECOND, 0);
	   cal.set(Calendar.MILLISECOND,0);
	   return cal.getTime();
   }
   public static Date nextDate(Date date) {
	   Calendar cal=Calendar.getInstance();
	   cal.setTime(date);
	   cal.add(Calendar.DAY_OF_MONTH,1);
	   return cal.getTime();
   }
   public static Date nextHour(Date date) {
	   Calendar cal=Calendar.getInstance();
	   cal.setTime(date);
	   cal.add(Calendar.HOUR_OF_DAY,1);
	   return cal.getTime();
   }
}
