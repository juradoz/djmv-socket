package net.danieljurado.socket;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class ServerPortTest {

	private static final int PORT = 1000;
	private ServerPort serverPort;

	@Before
	public void setUp() {
		serverPort = new ServerPort(PORT);
	}

	@Test(expected = IllegalArgumentException.class)
	public void construtorDeveriaLancarExceptionSeIntZero() {
		new ServerPort(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void construtorDeveriaLancarExceptionSeIntNegativo() {
		new ServerPort(Integer.MIN_VALUE);
	}

	@Test
	public void getServerPortDeveriaRetornarPort() {
		assertThat(serverPort.getServerPort(), is(equalTo(PORT)));
	}
}
