package com.taobao.timetunnel.client.broker;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import com.taobao.timetunnel.client.Message;
import com.taobao.timetunnel.client.message.MessageFactory;
import com.taobao.timetunnel.client.util.BytesUtil;
import com.taobao.timetunnel.thrift.gen.ExternalService;
import com.taobao.timetunnel.thrift.gen.Failure;

/**
 * 
 * @author <jiugao@taobao.com>
 * @created 2010-10-8
 * 
 */
public class BrokerImpl implements ExternalService.Iface {

	public static class Key {
		String topic;
		String token;
		String id;

		public Key(String topic, ByteBuffer token, String id) {
			this.topic = topic;
			this.token = new String(BytesUtil.toBytes(token), Charset.forName("utf-8"));
			this.id = id;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("topic: ").append(topic).append(" token: ").append(token).append(" id: ").append(id);
			return sb.toString();
		}

		@Override
		public int hashCode() {
			return toString().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Key))
				return false;
			Key that = (Key) obj;
			return this.toString().equals(that.toString());
		}
	}

	public ConcurrentHashMap<Key, Message> getReceived() {
		return received;
	}

	private static Logger log = Logger.getLogger(BrokerImpl.class);
	private ConcurrentHashMap<Key, Message> received = new ConcurrentHashMap<Key, Message>();
	private SendContent sc;

	public static interface SendContent {
		List<ByteBuffer> get2Send();
	}

	public void setContentGen(SendContent sc) {
		this.sc = sc;
	}

	@Override
	public List<ByteBuffer> ackAndGet(String category, ByteBuffer token) throws Failure, TException {
		List<ByteBuffer> get2Send = this.sc.get2Send();
		log.debug("Broker return messasge num is " + get2Send.size());
		return get2Send;
	}

	@Override
	public void post(String category, ByteBuffer token, ByteBuffer message) throws Failure, TException {
		Message m = MessageFactory.getInstance().createMessageFrom(BytesUtil.toBytes(message));
		Key k = new Key(category, token, m.getId());
		received.put(k, m);
		log.debug("Broker received: " + k.toString());
		return;
	}
}
