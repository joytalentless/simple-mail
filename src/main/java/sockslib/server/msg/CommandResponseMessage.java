package sockslib.server.msg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sockslib.utils.SocksUtil;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class CommandResponseMessage {

	private static final Logger logger = LoggerFactory.getLogger(CommandResponseMessage.class);

	public static byte[] getBytes(ServerReply reply) {
		return getBytes(reply, new InetSocketAddress(0).getAddress(), 0);
	}

	public static byte[] getBytes(ServerReply reply, InetAddress bindAddress, int bindPort) {
		int addressType = (bindAddress.getAddress().length == 4) ? 0x01 : 0x0;
		int version = sockslib.server.Socks5Handler.VERSION;

		final byte[] bytes = new byte[10];

		bytes[0] = (byte) version;
		bytes[1] = reply.getValue();
		bytes[2] = (byte) 0x00;
		bytes[3] = (byte) addressType;
		bytes[4] = bindAddress.getAddress()[0];
		bytes[5] = bindAddress.getAddress()[1];
		bytes[6] = bindAddress.getAddress()[2];
		bytes[7] = bindAddress.getAddress()[3];
		bytes[8] = SocksUtil.getFirstByteFromInt(bindPort);
		bytes[9] = SocksUtil.getSecondByteFromInt(bindPort);

		logger.trace("CommandResponseMessage.getBytes");
		return bytes;
	}

}
