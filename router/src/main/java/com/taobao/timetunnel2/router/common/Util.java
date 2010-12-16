package com.taobao.timetunnel2.router.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class Util {
	private static final Logger log = Logger.getLogger(Util.class);
	private static final Gson gson = new Gson();

	public static String toJsonStr(Object obj) {
		return gson.toJson(obj);
	}
	
	public static Object fromJson(String json, Class<?> cls){				
		return gson.fromJson(json, cls);
	}

	public static synchronized String getMD5(String s) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(s.getBytes());
			return toHexString(md5.digest());

		} catch (NoSuchAlgorithmException e) {
		}
		return "";
	}

	private static String toHexString(byte bytes[]) {
		if (bytes == null) {
			return null;
		}

		StringBuffer sb = new StringBuffer();
		for (int iter = 0; iter < bytes.length; iter++) {
			byte high = (byte) ((bytes[iter] & 0xf0) >> 4);
			byte low = (byte) (bytes[iter] & 0x0f);
			sb.append(nibble2char(high));
			sb.append(nibble2char(low));
		}

		return sb.toString();
	}

	private static char nibble2char(byte b) {
		byte nibble = (byte) (b & 0x0f);
		if (nibble < 10) {
			return (char) ('0' + nibble);
		}
		return (char) ('a' + nibble - 10);
	}
	
	public static int getIntParam(String param, String paramvalue) throws ValidationException{    	
    	return getIntParam(param, paramvalue, 0, 0, Integer.MAX_VALUE, true);
    }
	
	public static int getIntParam(String param, String paramvalue, int min, int max) throws ValidationException{    	
    	return getIntParam(param, paramvalue, 0, min, max, true);
    }
	
	public static int getIntParam(String param, String paramvalue, int defaultvalue, int min, int max) throws ValidationException{    	
    	return getIntParam(param, paramvalue, defaultvalue, min, max, false);
    }
	
    public static int getIntParam(String param, String paramvalue, int defaultvalue, int min, int max, boolean nullCheck) throws ValidationException{    	
    	if (paramvalue!=null){
    		try{
    			Integer value = Integer.valueOf(paramvalue);
    			if (value<min || value >max){
    				throw new ValidationException(String.format("The value[%s] of param[%s] is out of range{min=%d,max=%d}.",paramvalue, param, min, max));
    	    	}else
    	    		return value;    			
    		}catch(NumberFormatException e){
    			log.warn(String.format("Convert string to numeric types is failed[%s],so use the default value[%s] of param[%s].",paramvalue, defaultvalue, param));
    			return defaultvalue;
    		}
    	}else{
    		if (nullCheck)
    			throw new ValidationException(String.format("The param[%s] needs a required value.", param));
    		return defaultvalue;
    	}
    }
    
    public static long getLongParam(String param, String paramvalue, long defaultvalue, long min, long max) throws ValidationException{    	
    	if (paramvalue!=null){
    		try{
    			Long value = Long.valueOf(paramvalue);
    			if (value<min || value >max){
    				throw new ValidationException(String.format("The value[%s] of param[%s] is out of range{min=%d,max=%d}.",paramvalue, param, min, max));
    	    	}else
    	    		return value;    			
    		}catch(NumberFormatException e){
    			log.warn(String.format("Convert string to numeric types is failed[%s],so use the default value[%s] of param[%s].",paramvalue, defaultvalue, param));
    			return defaultvalue;
    		}
    	}else
    		return defaultvalue;
    }
    
    public static String getStrParam(String param, String paramvalue) throws ValidationException{
    	return getStrParam(param, paramvalue, null, true);
    }
    
    public static String getStrParam(String param, String paramvalue, String defaultvalue, boolean nullCheck) throws ValidationException{    	
    	if (paramvalue!=null){
    		return paramvalue;    
    	}else{
    		if (nullCheck && defaultvalue==null)
    			throw new ValidationException(String.format("The param[%s] needs a required value.", param));
    		return defaultvalue;
    	}
    }
}
