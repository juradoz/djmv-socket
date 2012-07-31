package net.danieljurado.socket;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class ServerPort {
	private final int port;

	public ServerPort(int port) {
		if (port <= 0)
			throw new IllegalArgumentException();
		this.port = port;
	}

	public int getServerPort() {
		return port;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("port", this.port).toString();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.port).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServerPort other = (ServerPort) obj;
		return new EqualsBuilder().append(this.port, other.port).isEquals();
	}
}
