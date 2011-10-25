package net.danieljurado.socket;

public interface SocketFactory {
	Socket create(java.net.Socket rawSocket);
}
