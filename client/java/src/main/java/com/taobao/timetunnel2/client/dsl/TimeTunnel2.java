package com.taobao.timetunnel2.client.dsl;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.taobao.timetunnel2.client.dsl.disk.AsyncQueue;
import com.taobao.timetunnel2.client.dsl.disk.DiskQueueFactory;
import com.taobao.timetunnel2.client.dsl.impl.Authentication;
import com.taobao.timetunnel2.client.dsl.impl.Config;
import com.taobao.timetunnel2.client.dsl.impl.SubscribeFutureImpl;
import com.taobao.timetunnel2.client.dsl.impl.Tunnel;
import com.taobao.timetunnel2.client.dsl.message.MessageFactory;
import com.taobao.timetunnel2.client.dsl.pub.PubTunnel;
import com.taobao.timetunnel2.client.dsl.pub.PubTunnelFactory;
import com.taobao.timetunnel2.client.dsl.sub.VirtualSubConnectionFactory;
import com.taobao.timetunnel2.client.dsl.url.ThriftUrls;
import com.taobao.timetunnel2.client.dsl.util.ClosedException;

/**
 * include most operations for interact with TimeTunnel
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-10-27
 * 
 */
public abstract class TimeTunnel2 {

	private static final Logger log = Logger.getLogger(TimeTunnel2.class);

	static class MessageAid {
		static Message message(final String topic, final byte[] bytes) {
			sanitize(topic, bytes);
			return MessageFactory.getInstance().createMessage(topic, bytes);
		}

		static Message message(final String topic, final String line) {
			return message(topic, line.getBytes(charset));
		}

		static Message message(final String topic, final byte[] content, final String ipAddress, final long createdTime, Map<String, String> props) {
			sanitize(topic, content);
			if (props == null)
				props = new HashMap<String, String>();
			return MessageFactory.getInstance().createMessage(topic, content, ipAddress, createdTime, props);
		}

		private static void sanitize(String topic, byte[] content) {
			if (content == null || topic == null)
				throw new NullPointerException();
			if (content.length > Config.getInstance().getMaxPostLen())
				throw new RuntimeException("message content too long");
		}
	}

	/**
	 * convert a message's content to String use default Charset
	 * 
	 * @param m
	 *            Message
	 * @return message's content in String
	 */
	public static String asString(Message m) {
		return new String(m.getContent(), charset);
	}

	/**
	 * convert a message's content to String by specified Charset
	 * 
	 * @param m
	 *            Message
	 * @param cs
	 *            Charset
	 * @return
	 */
	public static String asString(Message m, Charset cs) {
		return new String(m.getContent(), cs);
	}

	private static Charset charset = Charset.forName("utf-8");

	/**
	 * return in-use Charset
	 * 
	 * @return Charset
	 */
	public static Charset charset() {
		return charset;
	}

	/**
	 * use a Charset specified by name
	 * 
	 * @param name
	 */
	public static void charset(final String name) {
		if (name == null)
			throw new IllegalArgumentException("null charset name");
		charset = Charset.forName(name);
	}

	/**
	 * construct an authentication use for post/offer/sub
	 * 
	 * @param name
	 *            user's name
	 * @param password
	 *            user's password
	 * @return Authentication
	 */
	public static Authentication passport(final String name, final String password) {
		if (name == null || password == null)
			throw new IllegalArgumentException("null name or password");
		return new Authentication(name, password);
	}

	/**
	 * use an authentication in global
	 * 
	 * @param authentication
	 */
	public static void use(final Authentication authentication) {
		if (authentication == null)
			throw new IllegalArgumentException("use null authentication");
		ThriftUrls.getInstance().setAuth(authentication);
	}

	/**
	 * construct a tunnel for post/offer/sub later, the default property for
	 * this tunnel is un-compress, un-sequence, timeout equal to 1800s and max
	 * receive size 200
	 * 
	 * @param category
	 *            tunnel's name
	 * @return Tunnel
	 */
	public static Tunnel tunnel(final String category) {
		return new Tunnel(category, false, false, 0, 0);
	}

	/**
	 * construct a tunnel for post/offer/sub later, the default property for
	 * this tunnel is sequence false, timeout 1800s, max receive size 200
	 * 
	 * @param category
	 *            tunnel's name
	 * @param compress
	 *            message transfer via tunnel is compress or not
	 * @return Tunnel
	 */
	public static Tunnel tunnel(final String category, final boolean compress) {
		return new Tunnel(category, compress, false, 0, 0);
	}

	/**
	 * construct a tunnel for post/offer/sub later, the default property for
	 * timeout 1800s, max receive size 200
	 * 
	 * @param category
	 *            tunnel's name
	 * @param compress
	 *            message transfer via tunnel is compress or not
	 * @param sequence
	 *            should message keep sequence
	 * @return Tunnel
	 */
	public static Tunnel tunnel(final String category, final boolean compress, final boolean sequence) {
		return new Tunnel(category, compress, sequence, 0, 0);
	}

	/**
	 * construct a tunnel for post/offer/sub later, the default property for max
	 * receive size 200
	 * 
	 * @param category
	 *            tunnel's name
	 * @param compress
	 *            message transfer via tunnel is compress or not
	 * @param sequence
	 *            should message keep sequence
	 * @param timeout
	 *            heart-beat and unit is second
	 * @return Tunnel
	 */
	public static Tunnel tunnel(final String category, final boolean compress, final boolean sequence, int timeout) {
		return new Tunnel(category, compress, sequence, timeout, 0);
	}

	/**
	 * construct a tunnel for post/offer/sub later
	 * 
	 * @param category
	 *            tunnel's name
	 * @param compress
	 *            message transfer via tunnel is compress or not
	 * @param sequence
	 *            should message keep sequence
	 * @param timeout
	 *            heart-beat and unit is second
	 * @param maxRcvSize
	 *            for sub, max receive size once
	 * @return Tunnel
	 */
	public static Tunnel tunnel(final String category, final boolean compress, final boolean sequence, int timeout, int maxRcvSize) {
		return new Tunnel(category, compress, sequence, timeout, maxRcvSize);
	}

	/**
	 * Synchronized send message, return means message be sent successfully
	 * 
	 * @param content
	 *            string content to be sent
	 * @param tunnel
	 *            destination
	 * @throws ClosedException
	 *             thrown when message not been sent out and return, occur when
	 *             TimeTunnel.release called
	 */
	public static void post(final String content, final Tunnel tunnel) throws ClosedException {
		Message message = MessageAid.message(tunnel.getName(), content);
		post(message, tunnel);
	}

	/**
	 * Synchronized send message, return means message be sent successfully
	 * 
	 * @param bytes
	 *            byte content to be sent
	 * @param tunnel
	 *            destination
	 * @throws ClosedException
	 *             thrown when message not been sent out and return, occur when
	 *             TimeTunnel.release called
	 */
	public static void post(final byte[] bytes, final Tunnel tunnel) throws ClosedException {
		Message message = MessageAid.message(tunnel.getName(), bytes);
		post(message, tunnel);
	}

	/**
	 * Synchronized send message, return means message be sent successfully
	 * 
	 * @param content
	 *            byte content to be sent
	 * @param ipAddress
	 *            use defined ip address included in message
	 * @param createdTime
	 *            use defined time included in message
	 * @param props
	 *            use defined map included in message
	 * @param tunnel
	 *            destination
	 * @throws ClosedException
	 *             thrown when message not been sent out and return, occur when
	 *             TimeTunnel.release called
	 */
	public static void post(final byte[] content, final String ipAddress, final long createdTime, Map<String, String> props, final Tunnel tunnel) throws ClosedException {
		Message message = MessageAid.message(tunnel.getName(), content, ipAddress, createdTime, props);
		post(message, tunnel);
	}

	private static void post(final Message message, final Tunnel tunnel) throws ClosedException {
		if (stoped.get())
			throw new ClosedException("TimeTunnel has been closed", null);
		if (tunnel == null) {
			throw new IllegalArgumentException("null tunnel");
		}
		PubTunnel pt = PubTunnelFactory.getInstance().get(tunnel);
		assert (pt != null);
		if (tunnel.isCompress()) {
			message.compress();
		}
		pt.post(message);
	}

	/**
	 * Asynchronous send message, flow to disk directly, then return, Back-end
	 * thread used for sending message to Broker
	 * 
	 * @param content
	 *            string content to be sent
	 * @param tunnel
	 *            destination
	 * @throws ClosedException
	 *             thrown when message not been sent out and return, occur when
	 *             TimeTunnel.release called
	 */
	public static void offer(final String content, final Tunnel tunnel) throws ClosedException {
		Message message = MessageAid.message(tunnel.getName(), content);
		offer(message, tunnel);
	}

	/**
	 * Asynchronous send message, flow to disk directly, then return, Back-end
	 * thread used for sending message to Broker
	 * 
	 * @param bytes
	 *            byte content to be sent
	 * @param tunnel
	 *            destination
	 * @throws ClosedException
	 *             thrown when message not been sent out and return, occur when
	 *             TimeTunnel.release called
	 */
	public static void offer(final byte[] bytes, final Tunnel tunnel) throws ClosedException {
		Message message = MessageAid.message(tunnel.getName(), bytes);
		offer(message, tunnel);
	}

	/**
	 * Asynchronous send message, flow to disk directly, then return, Back-end
	 * thread used for sending message to Broker
	 * 
	 * @param content
	 *            byte content to be sent
	 * @param ipAddress
	 *            use defined ip address included in message
	 * @param createdTime
	 *            use defined time included in message
	 * @param props
	 *            use defined map included in message
	 * @param tunnel
	 *            destination
	 * @throws ClosedException
	 *             thrown when message not been sent out and return, occur when
	 *             TimeTunnel.release called
	 */
	public static void offer(final byte[] content, final String ipAddress, final long createdTime, Map<String, String> props, final Tunnel tunnel) throws ClosedException {
		Message message = MessageAid.message(tunnel.getName(), content, ipAddress, createdTime, props);
		offer(message, tunnel);
	}

	private static void offer(final Message message, final Tunnel tunnel) throws ClosedException {
		if (stoped.get())
			throw new ClosedException("release has been called", null);
		if (tunnel == null) {
			throw new IllegalArgumentException("null tunnel");
		}
		long begin = System.nanoTime();
		AsyncQueue asyncQ = DiskQueueFactory.getInstance().create(tunnel);
		assert (asyncQ != null);
		if (tunnel.isCompress()) {
			message.compress();
		}
		log.info("before add to asyncq elipse: " + (System.nanoTime() - begin));
		begin = System.nanoTime();
		asyncQ.add(message);
		log.info("add to asyncq elipse: " + (System.nanoTime() - begin));
	}

	/**
	 * subscribe a tunnel for receiving message from TimeTunnel
	 * 
	 * @param tunnel
	 *            a tunnel to subscribe
	 * @return future for this sub, use it to get message
	 * @throws ClosedException
	 *             if tunnel has been closed or release method called
	 */
	public static SubscribeFuture subscribe(final Tunnel tunnel) throws ClosedException {
		if (tunnel == null) {
			throw new IllegalArgumentException("null tunnel");
		}
		return new SubscribeFutureImpl(tunnel);
	}

	private static AtomicBoolean stoped = new AtomicBoolean(false);

	/**
	 * release all resource relate to TimeTunnel, can call explicit or call by
	 * runtime shutdown hook implicit
	 */
	public static void release() {
		if (!stoped.getAndSet(true)) {
			PubTunnelFactory.getInstance().destory();
			DiskQueueFactory.getInstance().destory();
			VirtualSubConnectionFactory.getInstance().destory();
		}
	}

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownJob(), "shutdown thread"));
	}

	static class ShutdownJob implements Runnable {
		@Override
		public void run() {
			release();
		}
	}
}
