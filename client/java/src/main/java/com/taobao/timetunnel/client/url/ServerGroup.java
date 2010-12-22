package com.taobao.timetunnel.client.url;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author <jiugao@taobao.com>
 * @created 2010-9-25
 * 
 */
public class ServerGroup {
	private final List<ServerUnit> serverUnits;
	private String stringRepr = null;
	private String token = null;

	public ServerGroup(List<ServerUnit> serverUnits) {
		this.serverUnits = serverUnits;
	}

	public ServerGroup(List<ServerUnit> serverUnits, String token) {
		this.serverUnits = serverUnits;
		this.token = token;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ServerGroup))
			return false;
		ServerGroup that = (ServerGroup) obj;
		return this.toString().equals(that.toString());
	}

	public Set<ServerUnit> notIn(ServerGroup sg) {
		Set<ServerUnit> ret = new HashSet<ServerUnit>();
		for (Iterator<ServerUnit> iterator = serverUnits.iterator(); iterator.hasNext();) {
			ServerUnit serverUnit = iterator.next();
			if (!sg.getServeUnits().contains(serverUnit)) {
				ret.add(serverUnit);
			}
		}
		return ret;
	}

	public Set<ServerUnit> in(ServerGroup sg) {
		Set<ServerUnit> ret = new HashSet<ServerUnit>();
		for (Iterator<ServerUnit> iterator = serverUnits.iterator(); iterator.hasNext();) {
			ServerUnit serverUnit = iterator.next();
			if (sg.getServeUnits().contains(serverUnit)) {
				ret.add(serverUnit);
			}
		}
		return ret;
	}

	public List<ServerUnit> getServeUnits() {
		return serverUnits;
	}

	public String getToken() {
		return token;
	}

	public ByteBuffer getTokenInSerialize() {
		return ByteBuffer.wrap(token.getBytes(Charset.forName("utf-8")));
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		if (stringRepr == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("token: ").append(token);
			sb.append(" ");
			for (Iterator<ServerUnit> iterator = serverUnits.iterator(); iterator.hasNext();) {
				ServerUnit pair = iterator.next();
				sb.append(pair.toString() + "#");
			}
			stringRepr = sb.toString();
		}
		return stringRepr;
	}

}
