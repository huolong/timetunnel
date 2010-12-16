package com.taobao.timetunnel2.router.exception;

public class ServiceException extends Exception {	

	private static final long serialVersionUID = 2012192565070851507L;

	public ServiceException() {
		super();
	}

	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceException(String message) {
		super(message);
	}

	public ServiceException(Throwable cause) {
		super(cause);
	}

}
