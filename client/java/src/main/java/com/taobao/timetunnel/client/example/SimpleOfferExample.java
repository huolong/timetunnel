package com.taobao.timetunnel.client.example;

import static com.taobao.timetunnel.client.TimeTunnel.*;

import com.taobao.timetunnel.client.util.ClosedException;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-12-22
 * 
 */
public class SimpleOfferExample {
	public static void main() {
		//the content to send
		String msg = "hello timetunnel, i am from a async offer";
		// use authenticate
		use(passport("username", "password"));
		// send content
		try {
			offer(msg, tunnel("topicname"));
		} catch (ClosedException e) {
			// tunnel has been closed or TimeTunnel.release() has been called
			// the current msg has not been flushed to disk
		}
	}
}
