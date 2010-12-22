package com.taobao.timetunnel.client.util;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-11-12
 * 
 */
public class ClosedException extends Exception {

	private static final long serialVersionUID = 5669680788553224153L;

	public ClosedException(String m, Throwable e) {
		super(m, e);
	}
}