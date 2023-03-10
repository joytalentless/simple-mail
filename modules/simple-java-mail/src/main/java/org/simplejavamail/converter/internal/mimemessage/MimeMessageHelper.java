package org.simplejavamail.converter.internal.mimemessage;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.Address;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimePart;
import jakarta.mail.internet.MimeUtility;
import jakarta.mail.internet.ParameterList;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.simplejavamail.api.email.AttachmentResource;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.email.Recipient;
import org.simplejavamail.api.mailer.config.EmailGovernance;
import org.simplejavamail.internal.util.MiscUtil;
import org.simplejavamail.internal.util.NamedDataSource;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static org.simplejavamail.internal.util.MiscUtil.orOther;
import static org.simplejavamail.internal.util.MiscUtil.orOtherList;
import static org.simplejavamail.internal.util.MiscUtil.valueNullOrEmpty;
import static org.simplejavamail.internal.util.Preconditions.checkNonEmptyArgument;

/**
 * Helper class that produces and populates a mime messages. Deals with jakarta.mail RFC MimeMessage stuff, as well as DKIM signing.
 */
public class MimeMessageHelper {

	/**
	 * Encoding used for setting body text, email address, headers, reply-to fields etc. ({@link StandardCharsets#UTF_8}).
	 */
	private static final String CHARACTER_ENCODING = StandardCharsets.UTF_8.name();

	private static final String HEADER_CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";

	private MimeMessageHelper() {

	}

	static void setSubject(@NotNull final Email email, final EmailGovernance governance, final MimeMessage message) throws MessagingException {
		final String subject = orOther(email, governance.getEmailDefaults(), governance.getEmailOverrides(), Email::getSubject);
		message.setSubject(subject, CHARACTER_ENCODING);
	}

	static void setFrom(@NotNull final Email email, final EmailGovernance governance, final MimeMessage message) throws UnsupportedEncodingException, MessagingException {
		val fromRecipient = orOther(email, governance.getEmailDefaults(), governance.getEmailOverrides(), Email::getFromRecipient);
		if (fromRecipient != null) {
			message.setFrom(new InternetAddress(fromRecipient.getAddress(), fromRecipient.getName(), CHARACTER_ENCODING));
		}
	}
	
	/**
	 * Fills the {@link Message} instance with recipients from the {@link Email}.
	 *
	 * @param email   The message in which the recipients are defined.
	 * @param message The javax message that needs to be filled with recipients.
	 * @throws UnsupportedEncodingException See {@link InternetAddress#InternetAddress(String, String)}.
	 * @throws MessagingException           See {@link Message#addRecipient(Message.RecipientType, Address)}
	 */
	static void setRecipients(final Email email, final EmailGovernance governance, final Message message)
			throws UnsupportedEncodingException, MessagingException {
		val recipients = orOtherList(email, governance.getEmailDefaults(), governance.getEmailOverrides(), Email::getRecipients);
		for (final Recipient recipient : recipients) {
				message.addRecipient(recipient.getType(), new InternetAddress(recipient.getAddress(), recipient.getName(), CHARACTER_ENCODING));
		}
	}

	/**
	 * Fills the {@link Message} instance with reply-to address.
	 *
	 * @param email   The message in which the recipients are defined.
	 * @param message The javax message that needs to be filled with reply-to address.
	 * @throws UnsupportedEncodingException See {@link InternetAddress#InternetAddress(String, String)}.
	 * @throws MessagingException           See {@link Message#setReplyTo(Address[])}
	 */
	static void setReplyTo(@NotNull final Email email, final EmailGovernance governance, final Message message)
			throws UnsupportedEncodingException, MessagingException {
		val replyToRecipient = orOther(email, governance.getEmailDefaults(), governance.getEmailOverrides(), Email::getReplyToRecipient);
		if (replyToRecipient != null) {
			final InternetAddress replyToAddress = new InternetAddress(replyToRecipient.getAddress(), replyToRecipient.getName(),
					CHARACTER_ENCODING);
			message.setReplyTo(new Address[] { replyToAddress });
		}
	}

	/**
	 * Fills the {@link Message} instance with the content bodies (text, html and calendar), with Content-Transfer-Encoding header taken from Email.
	 *
	 * @param email                        The message in which the content is defined.
	 * @param multipartAlternativeMessages See {@link MimeMultipart#addBodyPart(BodyPart)}
	 * @throws MessagingException See {@link BodyPart#setText(String)}, {@link BodyPart#setContent(Object, String)} and {@link MimeMultipart#addBodyPart(BodyPart)}.
	 */
	static void setTexts(@NotNull final Email email, final EmailGovernance governance, final MimeMultipart multipartAlternativeMessages)
			throws MessagingException {
		val plainText = orOther(email, governance.getEmailDefaults(), governance.getEmailOverrides(), Email::getPlainText);
		if (plainText != null) {
			final MimeBodyPart messagePart = new MimeBodyPart();
			messagePart.setText(plainText, CHARACTER_ENCODING);
			messagePart.addHeader(HEADER_CONTENT_TRANSFER_ENCODING, email.getContentTransferEncoding().getEncoder());
			multipartAlternativeMessages.addBodyPart(messagePart);
		}
		val htmlText = orOther(email, governance.getEmailDefaults(), governance.getEmailOverrides(), Email::getHTMLText);
		if (htmlText != null) {
			final MimeBodyPart messagePartHTML = new MimeBodyPart();
			messagePartHTML.setContent(htmlText, "text/html; charset=\"" + CHARACTER_ENCODING + "\"");
			messagePartHTML.addHeader(HEADER_CONTENT_TRANSFER_ENCODING, email.getContentTransferEncoding().getEncoder());
			multipartAlternativeMessages.addBodyPart(messagePartHTML);
		}
		val calendarText = orOther(email, governance.getEmailDefaults(), governance.getEmailOverrides(), Email::getCalendarText);
		val calendarMethod = orOther(email, governance.getEmailDefaults(), governance.getEmailOverrides(), Email::getCalendarMethod);
		if (calendarText != null && calendarMethod != null) {
			final MimeBodyPart messagePartCalendar = new MimeBodyPart();
			messagePartCalendar.setContent(calendarText, "text/calendar; charset=\"" + CHARACTER_ENCODING + "\"; method=\"" + calendarMethod + "\"");
			messagePartCalendar.addHeader(HEADER_CONTENT_TRANSFER_ENCODING, email.getContentTransferEncoding().getEncoder());
			multipartAlternativeMessages.addBodyPart(messagePartCalendar);
		}
	}

	/**
	 * Fills the {@link MimeBodyPart} instance with the content body content (text, html and calendar), with Content-Transfer-Encoding header taken from Email.
	 *
	 * @param email                   The message in which the content is defined.
	 * @param messagePart             The {@link MimeBodyPart} that will contain the body content (either plain text, HTML text or iCalendar text)
	 *
	 * @throws MessagingException See {@link BodyPart#setText(String)}, {@link BodyPart#setContent(Object, String)}.
	 */
	static void setTexts(@NotNull final Email email, final EmailGovernance governance, final MimePart messagePart)
			throws MessagingException {
		val plainText = orOther(email, governance.getEmailDefaults(), governance.getEmailOverrides(), Email::getPlainText);
		if (plainText != null) {
			messagePart.setText(plainText, CHARACTER_ENCODING);
		}
		val htmlText = orOther(email, governance.getEmailDefaults(), governance.getEmailOverrides(), Email::getHTMLText);
		if (htmlText != null) {
			messagePart.setContent(htmlText, "text/html; charset=\"" + CHARACTER_ENCODING + "\"");
		}
		val calendarText = orOther(email, governance.getEmailDefaults(), governance.getEmailOverrides(), Email::getCalendarText);
		val calendarMethod = orOther(email, governance.getEmailDefaults(), governance.getEmailOverrides(), Email::getCalendarMethod);
		if (calendarText != null && calendarMethod != null) {
			messagePart.setContent(calendarText, "text/calendar; charset=\"" + CHARACTER_ENCODING + "\"; method=\"" + calendarMethod + "\"");
		}
		messagePart.addHeader(HEADER_CONTENT_TRANSFER_ENCODING, email.getContentTransferEncoding().getEncoder());
	}
	
	/**
	 * If provided, adds the {@code emailToForward} as a MimeBodyPart to the mixed multipart root.
	 * <p>
	 * <strong>Note:</strong> this is done without setting {@code Content-Disposition} so email clients can choose
	 * how to display embedded forwards. Most client will show the forward as inline, some may show it as attachment.
	 */
	static void configureForwarding(@NotNull final Email email, final EmailGovernance governance, @NotNull final MimeMultipart multipartRootMixed) throws MessagingException {
		val emailToForward = orOther(email, governance.getEmailDefaults(), governance.getEmailOverrides(), Email::getEmailToForward);
		if (emailToForward != null) {
			final BodyPart fordwardedMessage = new MimeBodyPart();
			fordwardedMessage.setContent(emailToForward, "message/rfc822");
			multipartRootMixed.addBodyPart(fordwardedMessage);
		}
	}

	/**
	 * Fills the {@link Message} instance with the embedded images from the {@link Email}.
	 *
	 * @param email            The message in which the embedded images are defined.
	 * @param multipartRelated The branch in the email structure in which we'll stuff the embedded images.
	 * @throws MessagingException See {@link MimeMultipart#addBodyPart(BodyPart)} and {@link #getBodyPartFromDatasource(AttachmentResource, String)}
	 */
	static void setEmbeddedImages(@NotNull final Email email, final EmailGovernance governance, final MimeMultipart multipartRelated)
			throws MessagingException {
		val attachmentResources = orOtherList(email, governance.getEmailDefaults(), governance.getEmailOverrides(), Email::getEmbeddedImages);
		for (final AttachmentResource embeddedImage : attachmentResources) {
			multipartRelated.addBodyPart(getBodyPartFromDatasource(embeddedImage, Part.INLINE));
		}
	}


	/**
	 * Fills the {@link Message} instance with the attachments from the {@link Email}.
	 *
	 * @param email         The message in which the attachments are defined.
	 * @param multipartRoot The branch in the email structure in which we'll stuff the attachments.
	 * @throws MessagingException See {@link MimeMultipart#addBodyPart(BodyPart)} and {@link #getBodyPartFromDatasource(AttachmentResource, String)}
	 */
	static void setAttachments(@NotNull final Email email, final EmailGovernance governance, final MimeMultipart multipartRoot)
			throws MessagingException {
		val attachmentResources = orOtherList(email, governance.getEmailDefaults(), governance.getEmailOverrides(), Email::getAttachments);
		for (final AttachmentResource attachment : attachmentResources) {
			multipartRoot.addBodyPart(getBodyPartFromDatasource(attachment, Part.ATTACHMENT));
		}
	}

	/**
	 * Sets all headers on the {@link Message} instance. Since we're not using a high-level JavaMail method, the JavaMail library says we need to do
	 * some encoding and 'folding' manually, to get the value right for the headers (see {@link MimeUtility}.
	 * <p>
	 * Furthermore sets the notification flags <code>Disposition-Notification-To</code> and <code>Return-Receipt-To</code> if provided. It used
	 * JavaMail's built in method for producing an RFC compliant email address (see {@link InternetAddress#toString()}).
	 *
	 * @param email   The message in which the headers are defined.
	 * @param message The {@link Message} on which to set the raw, encoded and folded headers.
	 * @throws UnsupportedEncodingException See {@link MimeUtility#encodeText(String, String, String)}
	 * @throws MessagingException           See {@link Message#addHeader(String, String)}
	 * @see MimeUtility#encodeText(String, String, String)
	 * @see MimeUtility#fold(int, String)
	 */
	static void setHeaders(@NotNull final Email email, final EmailGovernance governance, final Message message)
			throws UnsupportedEncodingException, MessagingException {
		val collectedHeaders = new HashMap<String, Collection<String>>();
		if (governance.getEmailDefaults() != null) {
			addOrOverrideHeaders(collectedHeaders, governance.getEmailDefaults().getHeaders());
		}
		addOrOverrideHeaders(collectedHeaders, email.getHeaders());
		if (governance.getEmailOverrides() != null) {
			addOrOverrideHeaders(collectedHeaders, governance.getEmailOverrides().getHeaders());
		}
		// add headers (for raw message headers we need to 'fold' them using MimeUtility
		for (final Map.Entry<String, Collection<String>> header : collectedHeaders.entrySet()) {
			setHeader(message, header);
		}

		val useDispositionNotificationTo = orOther(email, governance.getEmailDefaults(), governance.getEmailOverrides(), Email::getUseDispositionNotificationTo);
		if (TRUE.equals(useDispositionNotificationTo)) {
			final Recipient dispositionTo = checkNonEmptyArgument(orOther(email, governance.getEmailDefaults(), governance.getEmailOverrides(), Email::getDispositionNotificationTo), "dispositionNotificationTo");
			final Address address = new InternetAddress(dispositionTo.getAddress(), dispositionTo.getName(), CHARACTER_ENCODING);
			message.setHeader("Disposition-Notification-To", address.toString());
		}

		val useReturnReceiptTo = orOther(email, governance.getEmailDefaults(), governance.getEmailOverrides(), Email::getUseReturnReceiptTo);
		if (TRUE.equals(useReturnReceiptTo)) {
			final Recipient returnReceiptTo = checkNonEmptyArgument(orOther(email, governance.getEmailDefaults(), governance.getEmailOverrides(), Email::getReturnReceiptTo), "returnReceiptTo");
			final Address address = new InternetAddress(returnReceiptTo.getAddress(), returnReceiptTo.getName(), CHARACTER_ENCODING);
			message.setHeader("Return-Receipt-To", address.toString());
		}
	}

	private static void addOrOverrideHeaders(HashMap<String, Collection<String>> collectedHeaders, @NotNull Map<String, Collection<String>> headers) {
		headers.forEach((headerKey, headerValues) -> {
			collectedHeaders.putIfAbsent(headerKey, new ArrayList<>());
			/*
				we don't merge header values that have the same key from defaults or overrides;
				instead, we assume the use will always want to override the entire header
			 */
			collectedHeaders.get(headerKey).clear();
			collectedHeaders.get(headerKey).addAll(headerValues);
		});
	}

	private static void setHeader(Message message, Map.Entry<String, Collection<String>> header) throws UnsupportedEncodingException, MessagingException {
		for (final String headerValue : header.getValue()) {
			final String headerName = header.getKey();
			final String headerValueEncoded = MimeUtility.encodeText(headerValue, CHARACTER_ENCODING, null);
			final String foldedHeaderValue = MimeUtility.fold(headerName.length() + 2, headerValueEncoded);
			message.addHeader(header.getKey(), foldedHeaderValue);
		}
	}

	/**
	 * Helper method which generates a {@link BodyPart} from an {@link AttachmentResource} (from its {@link DataSource}) and a disposition type
	 * ({@link Part#INLINE} or {@link Part#ATTACHMENT}). With this the attachment data can be converted into objects that fit in the email structure.
	 * <p>
	 * For every attachment and embedded image a header needs to be set.
	 *
	 * @param attachmentResource An object that describes the attachment and contains the actual content data.
	 * @param dispositionType    The type of attachment, {@link Part#INLINE} or {@link Part#ATTACHMENT} .
	 *
	 * @return An object with the attachment data read for placement in the email structure.
	 * @throws MessagingException All BodyPart setters.
	 */
	private static BodyPart getBodyPartFromDatasource(final AttachmentResource attachmentResource, final String dispositionType)
			throws MessagingException {
		final BodyPart attachmentPart = new MimeBodyPart();
		// setting headers isn't working nicely using the javax mail API, so let's do that manually
		final String resourceName = determineResourceName(attachmentResource, true);
		final String fileName = determineResourceName(attachmentResource, false);
		attachmentPart.setDataHandler(new DataHandler(new NamedDataSource(fileName, attachmentResource.getDataSource())));
		attachmentPart.setFileName(fileName);
		final String contentType = attachmentResource.getDataSource().getContentType();
		ParameterList pl = new ParameterList();
		pl.set("filename", fileName);
		pl.set("name", fileName);
		attachmentPart.setHeader("Content-Type", contentType + pl);
		attachmentPart.setHeader("Content-ID", format("<%s>", resourceName));
		attachmentPart.setHeader("Content-Description", attachmentResource.getDescription());
		if (!valueNullOrEmpty(attachmentResource.getContentTransferEncoding())) {
			attachmentPart.setHeader("Content-Transfer-Encoding", attachmentResource.getContentTransferEncoding().getEncoder());
		}
		attachmentPart.setDisposition(dispositionType);
		return attachmentPart;
	}

	/**
	 * Determines the right resource name and optionally attaches the correct extension to the name. The result is mime encoded.
	 */
	static String determineResourceName(final AttachmentResource attachmentResource, final boolean encodeResourceName) {
		final String datasourceName = attachmentResource.getDataSource().getName();

		String resourceName;

		if (!valueNullOrEmpty(attachmentResource.getName())) {
			resourceName = attachmentResource.getName();
		} else if (!valueNullOrEmpty(datasourceName)) {
			resourceName = datasourceName;
		} else {
			resourceName = "resource" + UUID.randomUUID();
		}
		if (!valueNullOrEmpty(datasourceName)) {
			resourceName = possiblyAddExtension(datasourceName, resourceName);
		}
		return encodeResourceName ? MiscUtil.encodeText(resourceName) : resourceName;
	}

	@NotNull
	private static String possiblyAddExtension(final String datasourceName, String resourceName) {
		@SuppressWarnings("UnnecessaryLocalVariable")
		final String possibleFilename = datasourceName;
		if (!resourceName.contains(".") && possibleFilename.contains(".")) {
			final String extension = possibleFilename.substring(possibleFilename.lastIndexOf("."));
			if (!resourceName.endsWith(extension)) {
				resourceName += extension;
			}
		}
		return resourceName;
	}
}