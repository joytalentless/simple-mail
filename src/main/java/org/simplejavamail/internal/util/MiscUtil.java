package org.simplejavamail.internal.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.mail.internet.MimeUtility;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.regex.Pattern;

import static java.lang.Integer.toHexString;
import static org.simplejavamail.internal.util.Preconditions.checkNonEmptyArgument;

public final class MiscUtil {

	private static final Pattern MATCH_INSIDE_CIDBRACKETS = Pattern.compile("<?([^>]*)>?");

	public static <T> T checkNotNull(final T value, final String msg) {
		if (value == null) {
			throw new NullPointerException(msg);
		}
		return value;
	}

	public static <T> T checkArgumentNotEmpty(final T value, final String msg) {
		if (valueNullOrEmpty(value)) {
			throw new IllegalArgumentException(msg);
		}
		return value;
	}

	public static <T> boolean valueNullOrEmpty(final T value) {
		return value == null ||
				(value instanceof String && ((String) value).isEmpty()) ||
				(value instanceof Collection && ((Collection<?>) value).isEmpty()) ||
				(value instanceof byte[] && ((byte[]) value).length == 0);
	}

	public static String buildLogString(final byte[] bytes, final boolean isReceived) {
		final StringBuilder debugMsg = new StringBuilder();
		debugMsg.append(isReceived ? "Received: " : "Sent: ");
		for (final byte aByte : bytes) {
			debugMsg.append(toHexString(toInt(aByte))).append(" ");
		}
		return debugMsg.toString();
	}

	public static int toInt(final byte b) {
		return b & 0xFF;
	}

	/**
	 * To make sure email clients can interpret text properly, we need to encode some values according to RFC-2047.
	 */
	public static String encodeText(@Nullable final String name) {
		if (name == null) {
			return null;
		}
		try {
			return MimeUtility.encodeText(name);
		} catch (final UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}

	@Nullable
	public static String extractCID(@Nullable final String cid) {
		return (cid != null) ? MATCH_INSIDE_CIDBRACKETS.matcher(cid).replaceAll("$1") : null;
	}

	/**
	 * Uses standard JDK java to read an inputstream to String using the given encoding (in {@link ByteArrayOutputStream#toString(String)}).
	 */
	@Nonnull
	public static String readInputStreamToString(@Nonnull final InputStream inputStream, @Nonnull final Charset charset)
			throws IOException {
		final BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int result = bufferedInputStream.read();
		while (result != -1) {
			byteArrayOutputStream.write((byte) result);
			result = bufferedInputStream.read();
		}
		return byteArrayOutputStream.toString(checkNonEmptyArgument(charset, "charset").name());
	}
	
	/**
	 * Recognizes the tails of each address entry, so it can replace the [';] delimiters, thereby disambiguating the delimiters, since they can
	 * appear in names as well (making it difficult to split on [,;] delimiters.
	 *
	 * @param emailAddressList The delimited list of addresses (or single address) optionally including the name.
	 * @return Array of address entries optionally including the names, trimmed for spaces or trailing delimiters.
	 */
	@Nonnull
	public static String[] extractEmailAddresses(@Nonnull final String emailAddressList) {
		// recognize value tails and replace the delimiters there, disambiguating delimiters
		return checkNonEmptyArgument(emailAddressList, "emailAddressList")
				.replaceAll("(@.*?>?)\\s*[,;]", "$1<|>")
				.replaceAll("<\\|>$", "") // remove trailing delimiter
				.split("\\s*<\\|>\\s*"); // split on delimiter including surround space
	}
	
	@Nullable
	public static <T> T defaultTo(@Nullable T value, @Nullable T defaultValue) {
		return value != null ? value : defaultValue;
	}
}