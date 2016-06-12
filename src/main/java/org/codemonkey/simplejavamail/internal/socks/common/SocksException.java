

package org.codemonkey.simplejavamail.internal.socks.common;

public class SocksException extends RuntimeException {

	private static final String serverReplyMessage[] = { "General SOCKS server failure", "Connection not allowed by ruleset",
			"Network " + "unreachable", "Host unreachable", "Connection refused", "TTL expired", "Command not " + "supported",
			"Address type not supported" };

	public SocksException(String msg) {
		super(msg);
	}

	public SocksException(String msg, Exception e) {
		super(msg, e);
	}

	public static SocksException serverReplyException(byte reply) {
		int code = reply;
		code = code & 0xff;
		if (code < 0 || code > 0x08) {
			return new SocksException("Unknown reply");
		}
		code = code - 1;
		return new SocksException(serverReplyMessage[code]);
	}

}
