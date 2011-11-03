package net.danieljurado.socket.impl;

import java.io.IOException;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import net.danieljurado.socket.ServerPort;
import net.danieljurado.socket.ServerSocket;
import net.danieljurado.socket.Socket;
import net.danieljurado.socket.SocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.assistedinject.Assisted;

public class ServerSocketImpl implements ServerSocket {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final ServerPort serverPort;
	private final SocketFactory socketFactory;
	private final SocketBinder socketBinder = new SocketBinder();
	private final SocketAccepter socketAccepter = new SocketAccepter();
	private final ExecutorService executor = Executors
			.newCachedThreadPool(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread thread = new Thread(r, "Socket-"
							+ r.getClass().getSimpleName());
					thread.setDaemon(true);
					return thread;
				}
			});

	private boolean ativo = true;
	private java.net.ServerSocket server;

	private final Set<Socket> sockets = Collections
			.synchronizedSet(new HashSet<Socket>());

	@Inject
	public ServerSocketImpl(@Assisted ServerPort serverPort,
			SocketFactory socketFactory) {
		this.serverPort = serverPort;
		this.socketFactory = socketFactory;

		Thread tSocketBinder = new Thread(socketBinder, socketBinder.getClass()
				.getSimpleName());
		tSocketBinder.setDaemon(false);
		tSocketBinder.start();

		Thread tSocketAccepter = new Thread(socketAccepter, socketAccepter
				.getClass().getSimpleName());
		tSocketAccepter.setDaemon(true);
		tSocketAccepter.start();
	}

	private class SocketBinder implements Runnable {

		@Override
		public void run() {
			do {
				try {
					server = new java.net.ServerSocket(
							serverPort.getServerPort());
					logger.info("Ready to accept connections...");
					do {
						java.net.Socket socket = server.accept();
						logger.info("New connection from {}", socket
								.getInetAddress().getHostAddress());
						socketAccepter.queue.offer(socket);
					} while (Thread.currentThread().isAlive());
				} catch (SocketException e) {
					if (!ativo)
						return;
					logger.error(e.getMessage(), e);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException f) {
						logger.error(f.getMessage(), f);
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException f) {
						logger.error(f.getMessage(), f);
					}
				}
			} while (Thread.currentThread().isAlive() && ativo);
		}
	}

	private class SocketAccepter implements Runnable {
		private final BlockingQueue<java.net.Socket> queue = new LinkedBlockingQueue<java.net.Socket>();

		@Override
		public void run() {
			do {
				try {
					java.net.Socket socket = queue.poll(1000,
							TimeUnit.MILLISECONDS);
					if (socket == null)
						continue;
					logger.debug("connect from {}", socket.getInetAddress()
							.getHostAddress());
					SocketImpl newSocket = (SocketImpl) socketFactory
							.create(socket);
					synchronized (sockets) {
						sockets.add(newSocket);
					}
					executor.execute(newSocket);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			} while (Thread.currentThread().isAlive() && ativo);
		}
	}

	@Override
	public void close() {
		this.ativo = false;
		try {
			this.server.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		executor.shutdown();
		synchronized (sockets) {
			for (Socket socket : sockets) {
				if (!socket.isConnected())
					continue;
				socket.disconnect();
			}
		}
	}

}
