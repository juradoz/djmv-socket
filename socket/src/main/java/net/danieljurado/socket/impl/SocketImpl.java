package net.danieljurado.socket.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import javax.inject.Inject;

import net.danieljurado.socket.Socket;
import net.danieljurado.socket.SocketProcessor;
import net.danieljurado.socket.SocketProcessorFactory;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.assistedinject.Assisted;

class SocketImpl implements Socket, Runnable {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final java.net.Socket rawSocket;
	private final BufferedReader bufferedreader;
	private final SocketProcessor socketProcessor;

	@Inject
	SocketImpl(@Assisted java.net.Socket rawSocket,
			SocketProcessorFactory socketProcessorFactory) {
		if (!rawSocket.isConnected())
			throw new IllegalStateException("socket is not connected");
		try {
			bufferedreader = new BufferedReader(new InputStreamReader(
					rawSocket.getInputStream()));
			this.rawSocket = rawSocket;
			this.socketProcessor = socketProcessorFactory.create(rawSocket);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void disconnect() {
		try {
			this.rawSocket.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isConnected() {
		return this.rawSocket.isConnected();
	}

	@Override
	public void run() {
		socketLifeCycle();
	}

	private void socketLifeCycle() {
		try {
			waitLines();
		} catch (SocketException e) {
			logger.warn(e.getMessage());
		} catch (SocketTimeoutException e) {
			logger.warn(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			logger.debug("Disconnect from {}", rawSocket.getInetAddress()
					.getHostName());
			this.socketProcessor.shutdown();
		}
	}

	private void waitLines() throws IOException {
		do {
			String message = bufferedreader.readLine();
			if (message == null)
				break;
			try {
				DateTime start = new DateTime();
				socketProcessor.process(message.trim());
				Duration duration = new Duration(start, new DateTime());
				if (duration.isLongerThan(new Duration(1000)))
					logger.warn("{} process runned for {}ms", message,
							duration.getMillis());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		} while (Thread.currentThread().isAlive());
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(this.rawSocket)
				.append(this.socketProcessor).toString();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.rawSocket)
				.append(this.socketProcessor).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SocketImpl other = (SocketImpl) obj;
		return new EqualsBuilder().append(this.rawSocket, other.rawSocket)
				.append(this.socketProcessor, other.socketProcessor).isEquals();
	}

}
