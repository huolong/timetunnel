package com.taobao.timetunnel.util;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class StringUtil {
	
	private static final String ENCODING="UTF-8";
	public static byte[] str2Bytes(String str) {
		try {
			return str.getBytes(ENCODING);
		}
		catch (UnsupportedEncodingException ignored){
			return new byte[0];
		}
	}
	public static String bytes2Str(byte[] bytes) {
		try {
			if (bytes==null)
				return "";
			return new String(bytes,ENCODING);
		}
		catch (UnsupportedEncodingException ignored){
			return "";
		}
	}
	public static String[] split(String s) {
		String[] entries=s.split("\\#");
		for (int i=0;i<entries.length;i++)
		  entries[i]=entries[i].trim();
		return entries;
	}
	public static String[] lastSplit(String s) {
		int p=s.lastIndexOf('#'); 
		if (p==-1)
			return new String[]{s,""};
		else
			return new String[]{s.substring(0,p).trim(),s.substring(p+1).trim()};
	}
	
	public static String concat(String ... args) {
		if (args==null || args.length==0)
			return "";
		StringBuffer sb=new StringBuffer(args[0]);
		for (int i=1;i<args.length;i++) {
			sb.append(" # ");
		    sb.append(args[i]);
		}
		return sb.toString();
	}
	public static List<String> filter(List<String> input,String key) {
	      List<String> output=new ArrayList<String>();
	      for (String line : input)
	         if (line.indexOf(key,-1)!=-1)
	             output.add(line);
	      return output;
	}
   
    public static String map2str(Map<String,String> map) {
    	StringBuilder sb=new StringBuilder();
    	for (String key : map.keySet()) {
    		sb.append(key);
    		sb.append("=");
    		sb.append(map.get(key));
    		sb.append("\n");
    	}
    	return sb.toString();
    }
    
}
