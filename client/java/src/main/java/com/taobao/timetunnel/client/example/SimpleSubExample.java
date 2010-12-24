package com.taobao.timetunnel.client.example;

import static com.taobao.timetunnel.client.TimeTunnel.asString;
import static com.taobao.timetunnel.client.TimeTunnel.passport;
import static com.taobao.timetunnel.client.TimeTunnel.subscribe;
import static com.taobao.timetunnel.client.TimeTunnel.tunnel;
import static com.taobao.timetunnel.client.TimeTunnel.use;

import java.util.Iterator;
import java.util.List;

import com.taobao.timetunnel.client.Message;
import com.taobao.timetunnel.client.SubscribeFuture;
import com.taobao.timetunnel.client.util.ClosedException;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-12-22
 * 
 */
public class SimpleSubExample {
	public static void main() {
		//use authenticate
		use(passport("username", "password"));
		//subscribe a topic
		SubscribeFuture subscriber = null;
		try {
			subscriber = subscribe(tunnel("topicname"));
		} catch (ClosedException e) {
			// tunnel has been closed or TimeTunnel.release() has been called
		}
		//retrieve message
		boolean stopFlag = true;
		while (stopFlag) {
			List<Message> ms = subscriber.get();
			for (Iterator<Message> it = ms.iterator(); it.hasNext();) {
				System.out.println(asString(it.next()));
			}
		}
		// close sub
		subscriber.cancel();
	}
}
