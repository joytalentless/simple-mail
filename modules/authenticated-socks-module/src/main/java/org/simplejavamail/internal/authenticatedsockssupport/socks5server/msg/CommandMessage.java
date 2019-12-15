/*
 * Copyright (C) 2009 Benny Bottema (benny@bennybottema.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.simplejavamail.internal.authenticatedsockssupport.socks5server.msg;

import org.simplejavamail.internal.authenticatedsockssupport.common.SocksException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CommandMessage {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommandMessage.class);

	private InetAddress inetAddress;

	private int port;

	private int command;

	private SocksServerReplyException socksServerReplyException;

	public void read(final InputStream inputStream)
			throws IOException {
		LOGGER.trace("CommandMessage.read");

		StreamUtil.checkEnd(inputStream.read()); // version, unused
		command = StreamUtil.checkEnd(inputStream.read());

		StreamUtil.checkEnd(inputStream.read());
		final int addressType = StreamUtil.checkEnd(inputStream.read());

		if (!AddressType.isSupport(addressType) && socksServerReplyException == null) {
			socksServerReplyException = new SocksServerReplyException(ServerReply.ADDRESS_TYPE_NOT_SUPPORTED);
		}

		// read address
		switch (addressType) {
			case AddressType.IPV4:
				final byte[] addressBytes = read(inputStream, 4);
				inetAddress = InetAddress.getByAddress(addressBytes);
				break;

			case AddressType.DOMAIN_NAME:
				final int domainLength = StreamUtil.checkEnd(inputStream.read());
				if (domainLength < 1) {
					throw new SocksException("Length of domain must great than 0");
				}
				final byte[] domainBytes = read(inputStream, domainLength);
				final String host = new String(domainBytes, UTF_8);
				try {
					inetAddress = InetAddress.getByName(host);
				} catch (final UnknownHostException e) {
					if (socksServerReplyException == null) {
						socksServerReplyException = new SocksServerReplyException(ServerReply.HOST_UNREACHABLE);
					}
				}
				break;
			default:
				// TODO Implement later.
				break;
		}

		port = bytesToInt(read(inputStream, 2));
	}

	private static int bytesToInt(final byte[] portBytes) {
		if (portBytes.length != 2) {
			throw new IllegalArgumentException("byte array size must be 2");
		}
		return ((portBytes[0] & 0xFF) << 8) | portBytes[1] & 0xFF;
	}

	private static byte[] read(final InputStream inputStream, final int length)
			throws IOException {
		final byte[] bytes = new byte[length];
		for (int i = 0; i < length; i++) {
			bytes[i] = (byte) StreamUtil.checkEnd(inputStream.read());
		}
		return bytes;
	}

	public boolean hasSocksException() {
		return socksServerReplyException != null;
	}

	public InetAddress getInetAddress() {
		return inetAddress;
	}

	public int getPort() {
		return port;
	}

	public int getCommand() {
		return command;
	}

	public SocksServerReplyException getSocksServerReplyException() {
		return socksServerReplyException;
	}

}
