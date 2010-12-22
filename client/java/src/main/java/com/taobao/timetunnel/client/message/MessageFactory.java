package com.taobao.timetunnel.client.message;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.taobao.timetunnel.client.Message;
import com.taobao.timetunnel.client.impl.Config;
import com.taobao.timetunnel.client.util.HostUtils;

public class MessageFactory {

	private final static MessageFactory instance = new MessageFactory();
	private final static Logger log = Logger.getLogger(MessageFactory.class);

	public static MessageFactory getInstance() {
		return instance;
	}

	private static CompresssAlgo ca = loadCompressAlgo();

	private static CompresssAlgo loadCompressAlgo() {
		try {
			String compressClassFullName = Config.getInstance().getCompressClassFullName();
			return (CompresssAlgo) Class.forName(compressClassFullName).newInstance();
		} catch (Exception e) {
			log.error("{}", e);
			throw new RuntimeException(e);
		}
	}
	
	public static CompresssAlgo getCa() {
		return ca;
	}

	private MessageFactory() {
	}

	public Message createMessage(String topic, byte[] content, String ipAddress, long createdTime, Map<String, String> props) {
		final String uuid = UUID.randomUUID().toString();
		return new MessageImpl(topic, uuid, content, ipAddress, createdTime, props);
	}

	public Message createMessage(String topic, byte[] content, String ipAddress, long createdTime) {
		return createMessage(topic, content, ipAddress, createdTime, new HashMap<String, String>());
	}

	public Message createMessage(String topic, byte[] content) {
		return createMessage(topic, content, HostUtils.hostname(), System.currentTimeMillis(), new HashMap<String, String>());
	}

	public Message createMessage(String topic, byte[] content, Map<String, String> props) {
		return createMessage(topic, content, HostUtils.hostname(), System.currentTimeMillis(), props);
	}

	public Message createMessageFrom(byte[] bytes) {
		Message message = createMessage(null, new byte[0], null, 0l);
		message.deserialize(bytes);
		return message;
	}

}
