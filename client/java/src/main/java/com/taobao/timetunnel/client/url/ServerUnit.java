package com.taobao.timetunnel.client.url;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author <jiugao@taobao.com>
 * @created 2010-9-25
 * 
 */
public class ServerUnit {
	private String master;
	private String[] slave;
	private volatile String repr;

	public ServerUnit(String master, String... slaves) {
		this.master = master;
		this.slave = slaves;
	}

	public ServerUnit() {
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ServerUnit)) {
			return false;
		}
		ServerUnit ms = (ServerUnit) obj;
		if (ms.slave.length != this.slave.length)
			return false;
		if (!ms.master.equalsIgnoreCase(master))
			return false;

		for (int i = 0; i < slave.length; i++) {
			if (!slave[i].equalsIgnoreCase(ms.slave[i]))
				return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	public String getPubMaster() {
		return master;
	}

	public List<String> getPubSlave() {
		if (slave.length == 0) {
			return new ArrayList<String>();
		}
		return Arrays.asList(slave);
	}

	/*
	 * reverse master slave, let sub connect to slave firstly, then to master
	 */
	public String getSubMaster() {
		return getPubMaster();
	}

	@Override
	public String toString() {
		if (repr == null) {
			StringBuffer sb = new StringBuffer();
			sb.append(master);
			for (String sl : slave) {
				sb.append("-");
				sb.append(sl);
			}
			repr = sb.toString();
		}
		return repr;
	}

	public int compareTo(ServerUnit serverUnit) {
		return this.toString().compareTo(serverUnit.toString());
	}

}
