package org.codemonkey.simplejavamail.socksserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BasicSocksProxyServer implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(BasicSocksProxyServer.class);

	private final ExecutorService threadPool = Executors.newFixedThreadPool(100);

	private final ServerSocket serverSocket;
	private boolean stop = false;

	public BasicSocksProxyServer(@SuppressWarnings("SameParameterValue") int port) {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void start() {
		new Thread(this).start();
	}

	public void stop() {
		stop = true;
		try {
			serverSocket.close();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public void run() {
		logger.info("Start proxy server at port:{}", serverSocket.getLocalPort());
		while (!stop) {
			try {
				logger.info("waiting for new connection...");
				Socket socket = serverSocket.accept();
				logger.trace("new session ----------------------------------------------------------------");
				socket.setSoTimeout(10000);
				threadPool.execute(new Socks5Handler(new SocksSession(socket)));
			} catch (IOException e) {
				if (e instanceof IOException && e.getMessage().equals("socket closed")) {
					logger.debug("socket closed");
				} else {
					throw new RuntimeException("server crashed...", e);
				}
			}
		}
		logger.debug("shutting down...");
		threadPool.shutdownNow();
	}
}