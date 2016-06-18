package org.simplejavamail.internal.util;

import static java.lang.Integer.toHexString;

public class MiscUtil {

	public static <T> T checkNotNull(T value, String msg) {
		if (value == null) {
			throw new NullPointerException(msg);
		}
		return value;
	}

	public static <T> T checkArgumentNotEmpty(T value, String msg) {
		if (valueNullOrEmpty(value)) {
			throw new IllegalArgumentException(msg);
		}
		return value;
	}

	public static <T> boolean valueNullOrEmpty(T value) {
		return value == null || (value instanceof String && ((String) value).isEmpty());
	}

	public static String buildLogString(byte[] bytes, boolean isReceived) {
		StringBuilder debugMsg = new StringBuilder();
		debugMsg.append(isReceived ? "Received: " : "Sent: ");
		for (byte aByte : bytes) {
			debugMsg.append(toHexString(toInt(aByte))).append(" ");
		}
		return debugMsg.toString();
	}

	public static int toInt(byte b) {
		return b & 0xFF;
	}
}
