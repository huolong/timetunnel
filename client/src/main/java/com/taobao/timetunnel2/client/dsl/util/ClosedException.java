package com.taobao.timetunnel2.client.dsl.util;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-11-12
 * 
 */
public class ClosedException extends RuntimeException {

	private static final long serialVersionUID = 2006190760478014400L;

	public ClosedException(String m, Throwable e) {
		super(m, e);
	}
}