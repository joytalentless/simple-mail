package sockslib.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sockslib.common.net.MonitorSocketWrapper;
import sockslib.common.net.NetworkMonitor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BasicSocksProxyServer implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(BasicSocksProxyServer.class);

	/**
	 * Thread pool used to process each connection.
	 */
	private final ExecutorService executorService = Executors.newFixedThreadPool(100);

	private ServerSocket serverSocket;

	@SuppressWarnings("FieldCanBeLocal")
	private Thread thread;

	private final NetworkMonitor networkMonitor = new NetworkMonitor();

	@Override
	public void run() {
		logger.info("Start proxy server at port:{}", 1080);
		//noinspection InfiniteLoopStatement
		while (true) {
			try {
				Socket socket = serverSocket.accept();
				socket = new MonitorSocketWrapper(socket, networkMonitor);
				socket.setSoTimeout(10000);
				SocksSession session = new SocksSession(socket);
				Socks5Handler socksHandler = new Socks5Handler();
				// initialize socks handler
				socksHandler.setSession(session);
				socksHandler.setBufferSize(1024 * 1024 * 5);

				executorService.execute(socksHandler);

			} catch (IOException e) {
				logger.debug(e.getMessage(), e);
			}
		}
	}

	public void start()
			throws IOException {
		serverSocket = new ServerSocket(1080);
		thread = new Thread(this);
		thread.setName("fs-thread");
		thread.setDaemon(false);
		thread.start();
	}
}