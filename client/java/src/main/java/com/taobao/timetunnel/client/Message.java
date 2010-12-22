package com.taobao.timetunnel.client;

import java.util.Set;

import com.taobao.timetunnel.client.message.Compressable;
import com.taobao.timetunnel.client.message.IOSerializable;

/**
 * object received from TimeTunnel, by call get method in SubscriberFuture
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-10-27
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
	 * @return meesage's source hostname
	 */
	String getIpAddress();

	/**
	 * 
	 * @return user defined properties in Set
	 */
	Set<String> getAllProperties();

	/**
	 * 
	 * @param key use defined key
	 * @return user defined property specified by key
	 */
	String getProperty(String key);

}
