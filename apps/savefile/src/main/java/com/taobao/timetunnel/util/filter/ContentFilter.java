package com.taobao.timetunnel.util.filter;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-12-20
 * 
 */
public interface ContentFilter {
	/**
	 * return null mean line will be discarded else the return string will be
	 * writen into file
	 * 
	 * @param line
	 *            line ends with newline flag
	 * @return
	 */
	public String filter(String line);
}
