package net.danieljurado.socket;

public interface SocketProcessor {

	void process(String message);

	void shutdown();

}
