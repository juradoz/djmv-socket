package net.danieljurado.socket;


public interface ServerSocketFactory {
	ServerSocket create(ServerPort serverPort);
}
