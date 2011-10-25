package net.danieljurado.socket;


public interface SocketProcessorFactory {
	SocketProcessor create(java.net.Socket rawSocket);
}
