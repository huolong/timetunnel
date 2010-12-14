package com.taobao.timetunnel2.client.dsl;

import java.util.Set;

import com.taobao.timetunnel2.client.dsl.message.Compressable;
import com.taobao.timetunnel2.client.dsl.message.IOSerializable;

/**
 * message object received from TimeTunnel SubscriberFuture
 * 
 * @author jiugao
 * 
 */
public interface Message extends IOSerializable, Compressable {
	/**
	 * 
	 * @return message's topic
	 */
	String getTopic();

	/**
	 * 
	 * @return message's uuid
	 */
	String getId();

	/**
	 * 
	 * @return message's content in bytes
	 */
	byte[] getContent();

	/**
	 * 
	 * @return message's create time
	 */
	long getCreatedTime();

	/**
	 * 
	 * @return meesage's source ip address
	 */
	String getIpAddress();

	/**
	 * 
	 * @return user defined properties in Set
	 */
	Set<String> getAllProperties();

	/**
	 * 
	 * @param key
	 * @return user defined property specified by key
	 */
	String getProperty(String key);

}
