package org.simplejavamail.converter;

import org.simplejavamail.converter.internal.mimemessage.MimeMessageHelper;
import org.simplejavamail.converter.internal.mimemessage.MimeMessageParser;
import org.simplejavamail.converter.internal.mimemessage.MimeMessageParser.ParsedMimeMessageComponents;
import org.simplejavamail.converter.internal.msgparser.OutlookMessageParser;
import org.simplejavamail.email.Email;
import org.simplejavamail.email.Recipient;
import org.simplejavamail.internal.util.MiscUtil;
import org.simplejavamail.outlookmessageparser.model.OutlookFileAttachment;
import org.simplejavamail.outlookmessageparser.model.OutlookMessage;
import org.simplejavamail.outlookmessageparser.model.OutlookRecipient;

import javax.activation.DataSource;
import javax.annotation.Nonnull;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.simplejavamail.converter.internal.mimemessage.MimeMessageHelper.produceMimeMessage;
import static org.simplejavamail.internal.util.MiscUtil.extractCID;
import static org.simplejavamail.internal.util.Preconditions.checkNonEmptyArgument;

/**
 * Utility to help convert {@link org.simplejavamail.email.Email} instances to other formats (MimeMessage, EML etc.) and vice versa.
 */
@SuppressWarnings("WeakerAccess")
public final class EmailConverter {

	private EmailConverter() {
		// util / helper class
	}

	/*
		To Email instance
	 */

	/**
	 * @param mimeMessage The MimeMessage from which to create the {@link Email}.
	 */
	public static Email mimeMessageToEmail(@Nonnull final MimeMessage mimeMessage) {
		checkNonEmptyArgument(mimeMessage, "mimeMessage");
		final Email email = new Email(false);
		fillEmailFromMimeMessage(email, MimeMessageParser.parseMimeMessage(mimeMessage));
		return email;
	}

	/**
	 * @param msgData The content of an Outlook (.msg) message from which to create the {@link Email}.
	 */
	public static Email outlookMsgToEmail(@Nonnull final String msgData) {
		final Email email = new Email(false);
		final OutlookMessage outlookMessage = OutlookMessageParser.parseOutlookMsg(checkNonEmptyArgument(msgData, "msgData"));
		fillEmailFromOutlookMessage(email, outlookMessage);
		return email;
	}

	/**
	 * @param msgfile The content of an Outlook (.msg) message from which to create the {@link Email}.
	 */
	public static Email outlookMsgToEmail(@Nonnull final File msgfile) {
		final Email email = new Email(false);
		final OutlookMessage outlookMessage = OutlookMessageParser.parseOutlookMsg(checkNonEmptyArgument(msgfile, "msgfile"));
		fillEmailFromOutlookMessage(email, outlookMessage);
		return email;
	}

	/**
	 * @param msgInputStream The content of an Outlook (.msg) message from which to create the {@link Email}.
	 */
	public static Email outlookMsgToEmail(@Nonnull final InputStream msgInputStream) {
		final Email email = new Email(false);
		final OutlookMessage outlookMessage = OutlookMessageParser.parseOutlookMsg(checkNonEmptyArgument(msgInputStream, "msgInputStream"));
		fillEmailFromOutlookMessage(email, outlookMessage);
		return email;
	}

	/**
	 * Delegates to {@link #emlToMimeMessage(String, Session)} using a dummy {@link Session} instance and passes the result to {@link
	 * #mimeMessageToEmail(MimeMessage)};
	 */
	public static Email emlToEmail(@Nonnull final String eml) {
		final MimeMessage mimeMessage = emlToMimeMessage(checkNonEmptyArgument(eml, "eml"), createDummySession());
		return mimeMessageToEmail(mimeMessage);
	}

	/*
		To MimeMessage instance
	 */

	/**
	 * @return Result of {@link #outlookMsgToEmail(String)} and {@link #emailToMimeMessage(Email)}
	 */
	@Nonnull
	public static MimeMessage outlookMsgToMimeMessage(@Nonnull final String outlookMsgData) {
		checkNonEmptyArgument(outlookMsgData, "outlookMsgData");
		return emailToMimeMessage(outlookMsgToEmail(outlookMsgData));
	}

	/**
	 * @return Result of {@link #outlookMsgToEmail(File)} and {@link #emailToMimeMessage(Email)}
	 */
	@Nonnull
	public static MimeMessage outlookMsgToMimeMessage(@Nonnull final File outlookMsgFile) {
		checkNonEmptyArgument(outlookMsgFile, "outlookMsgFile");
		return emailToMimeMessage(outlookMsgToEmail(outlookMsgFile));
	}

	/**
	 * @return Result of {@link #outlookMsgToEmail(InputStream)} and {@link #emailToMimeMessage(Email)}
	 */
	@Nonnull
	public static MimeMessage outlookMsgToMimeMessage(@Nonnull final InputStream outloookMsgInputStream) {
		checkNonEmptyArgument(outloookMsgInputStream, "outloookMsgInputStream");
		return emailToMimeMessage(outlookMsgToEmail(outloookMsgInputStream));
	}

	/**
	 * Delegates to {@link #emailToMimeMessage(Email, Session)}, using a new empty {@link Session} instance.
	 *
	 * @see #emailToMimeMessage(Email, Session)
	 */
	public static MimeMessage emailToMimeMessage(@Nonnull final Email email) {
		return emailToMimeMessage(checkNonEmptyArgument(email, "email"), createDummySession());
	}

	/**
	 * Refer to {@link MimeMessageHelper#produceMimeMessage(Email, Session)}
	 */
	public static MimeMessage emailToMimeMessage(@Nonnull final Email email, @Nonnull final Session session) {
		try {
			return produceMimeMessage(checkNonEmptyArgument(email, "email"), checkNonEmptyArgument(session, "session"));
		} catch (UnsupportedEncodingException | MessagingException e) {
			// this should never happen, so we don't acknowledge this exception (and simply bubble up)
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * Delegates to {@link #emlToMimeMessage(String, Session)} with an empty {@link Session} instance.
	 *
	 * @see #emailToMimeMessage(Email, Session)
	 */
	public static MimeMessage emlToMimeMessage(@Nonnull final String eml) {
		return emlToMimeMessage(checkNonEmptyArgument(eml, "eml"), createDummySession());
	}

	/**
	 * Relies on JavaMail's native parser of EML data, {@link MimeMessage#MimeMessage(Session, InputStream)}.
	 */
	public static MimeMessage emlToMimeMessage(@Nonnull final String eml, @Nonnull final Session session) {
		checkNonEmptyArgument(session, "session");
		checkNonEmptyArgument(eml, "eml");
		try {
			return new MimeMessage(session, new ByteArrayInputStream(eml.getBytes(UTF_8)));
		} catch (final MessagingException e) {
			throw new EmailConverterException(format(EmailConverterException.PARSE_ERROR_EML, e.getMessage()), e);
		}
	}

	/*
		To EML String
	 */

	/**
	 * @return The result of {@link MimeMessage#writeTo(OutputStream)} which should be in the standard EML format.
	 */
	public static String mimeMessageToEML(@Nonnull final MimeMessage mimeMessage) {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			checkNonEmptyArgument(mimeMessage, "mimeMessage").writeTo(os);
			return os.toString(UTF_8.name());
		} catch (IOException | MessagingException e) {
			// this should never happen, so we don't acknowledge this exception (and simply bubble up)
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * Delegates to {@link #emailToMimeMessage(Email)} and passes the result to {@link #mimeMessageToEML(MimeMessage)}.
	 *
	 * @see #emailToMimeMessage(Email, Session)
	 */
	public static String emailToEML(@Nonnull final Email email) {
		return mimeMessageToEML(emailToMimeMessage(checkNonEmptyArgument(email, "email")));
	}

	/**
	 * @return Result of {@link #outlookMsgToEmail(String)} and {@link #emailToEML(Email)}
	 */
	@Nonnull
	public static String outlookMsgToEML(@Nonnull final String outlookMsgData) {
		checkNonEmptyArgument(outlookMsgData, "outlookMsgData");
		return emailToEML(outlookMsgToEmail(outlookMsgData));
	}

	/**
	 * @return Result of {@link #outlookMsgToEmail(File)} and {@link #emailToEML(Email)}
	 */
	@Nonnull
	public static String outlookMsgToEML(@Nonnull final File outlookMsgFile) {
		checkNonEmptyArgument(outlookMsgFile, "outlookMsgFile");
		return emailToEML(outlookMsgToEmail(outlookMsgFile));
	}

	/**
	 * @return Result of {@link #outlookMsgToEmail(InputStream)} and {@link #emailToEML(Email)}
	 */
	@Nonnull
	public static String outlookMsgToEML(@Nonnull final InputStream outloookMsgInputStream) {
		checkNonEmptyArgument(outloookMsgInputStream, "outloookMsgInputStream");
		return emailToEML(outlookMsgToEmail(outloookMsgInputStream));
	}

	/*
		Helpers
	 */

	private static void fillEmailFromMimeMessage(@Nonnull final Email email, @Nonnull final ParsedMimeMessageComponents parsed) {
		checkNonEmptyArgument(email, "email");
		checkNonEmptyArgument(parsed, "parsedMimeMessageComponents");
		if (parsed.getFromAddress() != null) {
			email.setFromAddress(parsed.getFromAddress().getPersonal(), parsed.getFromAddress().getAddress());
		}
		if (parsed.getReplyToAddresses() != null) {
			email.setReplyToAddress(parsed.getReplyToAddresses().getPersonal(), parsed.getReplyToAddresses().getAddress());
		}
		for (final Map.Entry<String, Object> header : parsed.getHeaders().entrySet()) {
			email.addHeader(header.getKey(), header.getValue());
		}
		final InternetAddress dnTo = parsed.getDispositionNotificationTo();
		if (dnTo != null) {
			email.setDispositionNotificationTo(new Recipient(dnTo.getPersonal(), dnTo.getAddress(), null));
		}
		final InternetAddress rrTo = parsed.getReturnReceiptTo();
		if (rrTo != null) {
			email.setReturnReceiptTo(new Recipient(rrTo.getPersonal(), rrTo.getAddress(), null));
		}
		final InternetAddress bTo = parsed.getBounceToAddress();
		if (bTo != null) {
			email.setBounceToRecipient(new Recipient(bTo.getPersonal(), bTo.getAddress(), null));
		}
		email.setId(parsed.getMessageId());
		for (final InternetAddress to : parsed.getToAddresses()) {
			email.addNamedToRecipients(to.getPersonal(), to.getAddress());
		}
		//noinspection QuestionableName
		for (final InternetAddress cc : parsed.getCcAddresses()) {
			email.addNamedCcRecipients(cc.getPersonal(), cc.getAddress());
		}
		for (final InternetAddress bcc : parsed.getBccAddresses()) {
			email.addNamedBccRecipients(bcc.getPersonal(), bcc.getAddress());
		}
		email.setSubject(parsed.getSubject() != null ? parsed.getSubject() : "");
		email.setText(parsed.getPlainContent());
		email.setTextHTML(parsed.getHtmlContent());
		for (final Map.Entry<String, DataSource> cid : parsed.getCidMap().entrySet()) {
			final String cidName = checkNonEmptyArgument(cid.getKey(), "cid.key");
			email.addEmbeddedImage(extractCID(cidName), cid.getValue());
		}
		for (final Map.Entry<String, DataSource> attachment : parsed.getAttachmentList().entrySet()) {
			email.addAttachment(extractCID(attachment.getKey()), attachment.getValue());
		}
	}
	
	private static void fillEmailFromOutlookMessage(@Nonnull final Email email, @Nonnull final OutlookMessage outlookMessage) {
		checkNonEmptyArgument(email, "email");
		checkNonEmptyArgument(outlookMessage, "outlookMessage");
		email.setFromAddress(outlookMessage.getFromName(), outlookMessage.getFromEmail());
		if (!MiscUtil.valueNullOrEmpty(outlookMessage.getReplyToEmail())) {
			email.setReplyToAddress(outlookMessage.getReplyToName(), outlookMessage.getReplyToEmail());
		}
		for (final OutlookRecipient to : outlookMessage.getRecipients()) {
			email.addNamedToRecipients(to.getName(), to.getAddress());
		}
		//noinspection QuestionableName
		for (final OutlookRecipient cc : outlookMessage.getCcRecipients()) {
			email.addNamedCcRecipients(cc.getName(), cc.getAddress());
		}
		for (final OutlookRecipient bcc : outlookMessage.getBccRecipients()) {
			email.addNamedBccRecipients(bcc.getName(), bcc.getAddress());
		}
		email.setSubject(outlookMessage.getSubject());
		email.setText(outlookMessage.getBodyText());
		email.setTextHTML(outlookMessage.getBodyHTML() != null ? outlookMessage.getBodyHTML() : outlookMessage.getConvertedBodyHTML());

		for (final Map.Entry<String, OutlookFileAttachment> cid : outlookMessage.fetchCIDMap().entrySet()) {
			final String cidName = checkNonEmptyArgument(cid.getKey(), "cid.key");
			//noinspection ConstantConditions
			email.addEmbeddedImage(extractCID(cidName), cid.getValue().getData(), cid.getValue().getMimeTag());
		}
		for (final OutlookFileAttachment attachment : outlookMessage.fetchTrueAttachments()) {
			email.addAttachment(attachment.getLongFilename(), attachment.getData(), attachment.getMimeTag());
		}
	}

	private static Session createDummySession() {
		return Session.getDefaultInstance(new Properties());
	}
}