package com.taobao.timetunnel.client.impl;

/**
 * 
 * @author <jiugao@taobao.com>
 * @created 2010-9-29
 * 
 */
public class Tunnel {
	private final String name;
	private final boolean compress;
	private final int maxRcvSize;
	private final int timeout;
	private final boolean sequence;

	public Tunnel(String name, boolean compress, boolean sequence, int timeout, int maxRcvSize) {
		if (name == null || name.trim().equalsIgnoreCase("")) {
			throw new IllegalArgumentException("tunnel name is null");
		}
		this.name = name.trim();
		this.compress = compress;
		this.sequence = sequence;
		if (timeout == 0)
			this.timeout = 1800;
		else
			this.timeout = timeout;
		if (maxRcvSize == 0)
			this.maxRcvSize = 200;
		else
			this.maxRcvSize = maxRcvSize;
	}

	public String getName() {
		return name;
	}

	public boolean isCompress() {
		return compress;
	}

	public int getMaxRcvSize() {
		return maxRcvSize;
	}

	public int getTimeout() {
		return timeout;
	}

	public boolean isSequence() {
		return sequence;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Tunnel))
			return false;
		return this.name.equals(((Tunnel) obj).name);
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Tunnel: ").append(this.name);
		sb.append(" compress: ").append(this.compress);
		sb.append(" maxRcvSize: ").append(this.maxRcvSize);
		sb.append(" timeout: ").append(this.timeout);
		sb.append(" sequence: ").append(this.sequence);
		return sb.toString();

	}

	public static void main(String[] args) {
		System.out.println(new Tunnel("t2", true, false, 0, 300));
	}
}
