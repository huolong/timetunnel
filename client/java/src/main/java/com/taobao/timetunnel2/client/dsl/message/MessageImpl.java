package com.taobao.timetunnel2.client.dsl.message;

import java.nio.ByteBuffer;
import java.util.*;

import org.apache.log4j.Logger;
import org.apache.thrift.*;
import org.apache.thrift.protocol.*;

import com.taobao.timetunnel2.client.dsl.Message;
import com.taobao.timetunnel2.client.dsl.util.BytesUtil;

public class MessageImpl implements Message {
	private static final Logger log = Logger.getLogger(MessageImpl.class);
	private TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
	private TDeserializer deserializer = new TDeserializer(new TBinaryProtocol.Factory());
	private MessageStruct message;

	protected MessageImpl(String topic, String id, byte[] content, String ipAddress, long createdTime, Map<String, String> props) {
		message = new MessageStruct(topic, ByteBuffer.wrap(content), createdTime, id, ipAddress, props);
	}

	public String getTopic() {
		return message.topic;
	}

	public void setTopic(String topic) {
		message.topic = topic;
	}

	public byte[] getContent() {
		return BytesUtil.toBytes(message.content);
	}

	public long getCreatedTime() {
		return message.createdTime;
	}

	public String getId() {
		return message.id;
	}

	public String getIpAddress() {
		return message.ipAddress;
	}

	public Set<String> getAllProperties() {
		return message.props.keySet();
	}

	public String getProperty(String key) {
		return message.props.get(key);
	}

	public void setProperty(String key, String value) {
		message.props.put(key, value);
	}

	public synchronized void deserialize(byte[] bytes) {
		try {
			deserializer.deserialize(this.message, bytes);
		} catch (TException e) {
			throw new RuntimeException("Fail to deserialize message using thrift", e);
		}
	}

	public synchronized byte[] serialize() {
		try {
			return serializer.serialize(this.message);
		} catch (TException e) {
			throw new RuntimeException("Fail to deserialize message using thrift", e);
		}
	}

	@Override
	public synchronized void compress() {
		if (isCompressed())
			return;
		message.content = ByteBuffer.wrap(MessageFactory.getCa().compress(message.content.array()));
		setCompressAlgo(MessageFactory.getCa().algoName());
		setCompressed(true);
	}

	private void setCompressAlgo(String algoName) {
		setProperty(MessagePropKeyEnum.COMPRESSALGO.getToken(), algoName);
	}

	private void clearCompressAlgo() {
		message.props.remove(MessagePropKeyEnum.COMPRESSALGO.getToken());
	}

	private String getCompressAlgo() {
		return getProperty(MessagePropKeyEnum.COMPRESSALGO.getToken());
	}

	@Override
	public synchronized void decompress() {
		if (!isCompressed())
			return;
		if (!getCompressAlgo().equalsIgnoreCase(MessageFactory.getCa().algoName())) {
			log.error("can not decompress, de/compress algo not matched: " + MessageFactory.getCa().algoName() + " vs " + getCompressAlgo());
			throw new RuntimeException("can not decompress, de/compress algo not matched");
		}
		message.content = ByteBuffer.wrap(MessageFactory.getCa().decompress(message.content.array()));
		setCompressed(false);
		clearCompressAlgo();
	}

	@Override
	public synchronized boolean isCompressed() {
		return MessagePropValueEnum.YES.getToken().equals(getProperty(MessagePropKeyEnum.COMPRESSED.getToken()));
	}

	private void setCompressed(boolean isCompressed) {
		if (isCompressed)
			setProperty(MessagePropKeyEnum.COMPRESSED.getToken(), MessagePropValueEnum.YES.getToken());
		else
			setProperty(MessagePropKeyEnum.COMPRESSED.getToken(), MessagePropValueEnum.NO.getToken());
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !other.getClass().equals(getClass()))
			return false;
		return Arrays.equals(this.serialize(), ((MessageImpl) other).serialize());
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.serialize());
	}

	@Override
	public String toString() {
		return message.topic + "#" + message.id;
	}

}
