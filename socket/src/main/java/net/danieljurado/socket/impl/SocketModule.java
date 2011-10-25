package net.danieljurado.socket.impl;

import net.danieljurado.socket.ServerSocket;
import net.danieljurado.socket.ServerSocketFactory;
import net.danieljurado.socket.Socket;
import net.danieljurado.socket.SocketFactory;
import net.danieljurado.socket.SocketProcessorFactory;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public abstract class SocketModule extends AbstractModule {

	@Override
	protected final void configure() {
		install(new FactoryModuleBuilder().implement(Socket.class,
				SocketImpl.class).build(SocketFactory.class));
		install(new FactoryModuleBuilder().implement(ServerSocket.class,
				ServerSocketImpl.class).build(ServerSocketFactory.class));
		bind(SocketProcessorFactory.class).to(bindSocketProcessorFactory());
	}

	protected abstract Class<? extends SocketProcessorFactory> bindSocketProcessorFactory();

}
