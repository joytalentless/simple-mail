package org.simplejavamail.email;

import org.simplejavamail.converter.EmailConverter;
import org.simplejavamail.converter.internal.mimemessage.MimeMessageParser;
import org.simplejavamail.internal.util.MiscUtil;

import javax.activation.DataSource;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.regex.Pattern.compile;
import static org.simplejavamail.internal.util.MiscUtil.defaultTo;
import static org.simplejavamail.internal.util.MiscUtil.extractEmailAddresses;
import static org.simplejavamail.internal.util.MiscUtil.valueNullOrEmpty;
import static org.simplejavamail.internal.util.Preconditions.checkNonEmptyArgument;
import static org.simplejavamail.util.ConfigLoader.Property.DEFAULT_BCC_ADDRESS;
import static org.simplejavamail.util.ConfigLoader.Property.DEFAULT_BCC_NAME;
import static org.simplejavamail.util.ConfigLoader.Property.DEFAULT_BOUNCETO_ADDRESS;
import static org.simplejavamail.util.ConfigLoader.Property.DEFAULT_BOUNCETO_NAME;
import static org.simplejavamail.util.ConfigLoader.Property.DEFAULT_CC_ADDRESS;
import static org.simplejavamail.util.ConfigLoader.Property.DEFAULT_CC_NAME;
import static org.simplejavamail.util.ConfigLoader.Property.DEFAULT_FROM_ADDRESS;
import static org.simplejavamail.util.ConfigLoader.Property.DEFAULT_FROM_NAME;
import static org.simplejavamail.util.ConfigLoader.Property.DEFAULT_REPLYTO_ADDRESS;
import static org.simplejavamail.util.ConfigLoader.Property.DEFAULT_REPLYTO_NAME;
import static org.simplejavamail.util.ConfigLoader.Property.DEFAULT_SUBJECT;
import static org.simplejavamail.util.ConfigLoader.Property.DEFAULT_TO_ADDRESS;
import static org.simplejavamail.util.ConfigLoader.Property.DEFAULT_TO_NAME;
import static org.simplejavamail.util.ConfigLoader.getProperty;
import static org.simplejavamail.util.ConfigLoader.hasProperty;

/**
 * Fluent interface Builder for Emails
 *
 * @author Jared Stewart, Benny Bottema
 */
@SuppressWarnings("UnusedReturnValue")
public class EmailBuilderOld {

	/**
	 * List of {@link Recipient}.
	 */
	private final Set<Recipient> recipients;

	/**
	 * List of {@link AttachmentResource}.
	 */
	private final List<AttachmentResource> embeddedImages;

	/**
	 * List of {@link AttachmentResource}.
	 */
	private final List<AttachmentResource> attachments;

	/**
	 * Map of header name and values, such as <code>X-Priority</code> etc.
	 */
	private final Map<String, String> headers;

	/**
	 * A file reference to the private key to be used for signing with DKIM.
	 */
	private File dkimPrivateKeyFile;

	/**
	 * An input stream containg the private key data to be used for signing with DKIM.
	 */
	private InputStream dkimPrivateKeyInputStream;

	/**
	 * The domain used for signing with DKIM.
	 */
	private String signingDomain;

	/**
	 * The dkimSelector to be used in combination with the domain.
	 */
	private String dkimSelector;

	/**
	 * Indicates the new emails should set the <a href="https://tools.ietf.org/html/rfc8098">NPM flag "Disposition-Notification-To"</a>. This flag can
	 * be used to request a return receipt from the recipient to signal that the recipient has read the email.
	 * <p>
	 * This flag may be ignored by SMTP clients (for example gmail ignores it completely, while the Google Apps business suite honors it).
	 * <p>
	 * If no address is provided, {@link #dispositionNotificationTo} will default to {@link #replyToRecipient} if available or else
	 * {@link #fromRecipient}.
	 */
	private boolean useDispositionNotificationTo;

	/**
	 * @see #useDispositionNotificationTo
	 */
	private Recipient dispositionNotificationTo;

	/**
	 * Indicates the new emails should set the <a href="https://en.wikipedia.org/wiki/Return_receipt">RRT flag "Return-Receipt-To"</a>. This flag
	 * can be used to request a notification from the SMTP server recipient to signal that the recipient has read the email.
	 * <p>
	 * This flag is rarely used, but your mail server / client might implement this flag to automatically send back a notification that the email
	 * was received on the mail server or opened in the client, depending on the chosen implementation.
	 * <p>
	 * If no address is provided, {@link #returnReceiptTo} will default to {@link #replyToRecipient} if available or else {@link #fromRecipient}.
	 */
	private boolean useReturnReceiptTo;

	/**
	 * @see #useReturnReceiptTo
	 */
	private Recipient returnReceiptTo;

	/**
	 * Holds a message that should be included in the new email as forwarded message.
	 */
	private MimeMessage emailToForward;

	public EmailBuilderOld() {
		recipients = new HashSet<>();
		embeddedImages = new ArrayList<>();
		attachments = new ArrayList<>();
		headers = new HashMap<>();

		if (hasProperty(DEFAULT_FROM_ADDRESS)) {
			from((String) getProperty(DEFAULT_FROM_NAME), (String) getProperty(DEFAULT_FROM_ADDRESS));
		}
		if (hasProperty(DEFAULT_REPLYTO_ADDRESS)) {
			replyTo((String) getProperty(DEFAULT_REPLYTO_NAME), (String) getProperty(DEFAULT_REPLYTO_ADDRESS));
		}
		if (hasProperty(DEFAULT_BOUNCETO_ADDRESS)) {
			bounceTo((String) getProperty(DEFAULT_BOUNCETO_NAME), (String) getProperty(DEFAULT_BOUNCETO_ADDRESS));
		}
		if (hasProperty(DEFAULT_TO_ADDRESS)) {
			if (hasProperty(DEFAULT_TO_NAME)) {
				to((String) getProperty(DEFAULT_TO_NAME), (String) getProperty(DEFAULT_TO_ADDRESS));
			} else {
				to((String) getProperty(DEFAULT_TO_ADDRESS));
			}
		}
		if (hasProperty(DEFAULT_CC_ADDRESS)) {
			if (hasProperty(DEFAULT_CC_NAME)) {
				cc((String) getProperty(DEFAULT_CC_NAME), (String) getProperty(DEFAULT_CC_ADDRESS));
			} else {
				cc((String) getProperty(DEFAULT_CC_ADDRESS));
			}
		}
		if (hasProperty(DEFAULT_BCC_ADDRESS)) {
			if (hasProperty(DEFAULT_BCC_NAME)) {
				bcc((String) getProperty(DEFAULT_BCC_NAME), (String) getProperty(DEFAULT_BCC_ADDRESS));
			} else {
				bcc((String) getProperty(DEFAULT_BCC_ADDRESS));
			}
		}
		if (hasProperty(DEFAULT_SUBJECT)) {
			subject((String) getProperty(DEFAULT_SUBJECT));
		}
	}

	public Email build() {
		return new Email(this);
	}

	/**
	 * Sets the optional id to be used when sending using the underlying Java Mail framework. Will be generated otherwise.
	 */
	public EmailBuilderOld id(@Nullable final String id) {
		this.id = id;
		return this;
	}

	/**
	 * Sets the sender address {@link #fromRecipient}.
	 *
	 * @param fromAddress The sender's email address.
	 */
	public EmailBuilderOld from(@Nonnull final String fromAddress) {
		return from(null, fromAddress);
	}

	/**
	 * Sets the sender address {@link #fromRecipient}.
	 *
	 * @param name        The sender's name.
	 * @param fromAddress The sender's email address.
	 */
	public EmailBuilderOld from(@Nullable final String name, @Nonnull final String fromAddress) {
		checkNonEmptyArgument(fromAddress, "fromAddress");
		this.fromRecipient = new Recipient(name, fromAddress, null);
		return this;
	}

	/**
	 * Sets the sender address {@link #fromRecipient} with preconfigured {@link Recipient}.
	 *
	 * @param recipient Preconfigured recipient (name is optional).
	 */
	public EmailBuilderOld from(@Nonnull final Recipient recipient) {
		checkNonEmptyArgument(recipient, "recipient");
		this.fromRecipient = new Recipient(recipient.getName(), recipient.getAddress(), null);
		return this;
	}

	/**
	 * Sets {@link #replyToRecipient} (optional).
	 *
	 * @param name           The replied-to-receiver name.
	 * @param replyToAddress The replied-to-receiver email address.
	 */
	public EmailBuilderOld replyTo(@Nullable final String name, @Nonnull final String replyToAddress) {
		checkNonEmptyArgument(replyToAddress, "replyToAddress");
		this.replyToRecipient = new Recipient(name, replyToAddress, null);
		return this;
	}

	/**
	 * Sets {@link #bounceToRecipient} (optional).
	 *
	 * @param name            The name of the bouncing emails receiver.
	 * @param bounceToAddress The address of the bouncing emails receiver.
	 */
	public EmailBuilderOld bounceTo(@Nullable final String name, @Nonnull final String bounceToAddress) {
		checkNonEmptyArgument(bounceToAddress, "bounceToAddress");
		this.bounceToRecipient = new Recipient(name, bounceToAddress, null);
		return this;
	}

	/**
	 * Sets {@link #replyToRecipient} (optional) with preconfigured {@link Recipient}.
	 *
	 * @param recipient Preconfigured recipient (name is optional).
	 */
	public EmailBuilderOld replyTo(@Nonnull final Recipient recipient) {
		checkNonEmptyArgument(recipient, "replyToRecipient");
		this.replyToRecipient = new Recipient(recipient.getName(), recipient.getAddress(), null);
		return this;
	}

	/**
	 * Sets {@link #bounceToRecipient} (optional) with preconfigured {@link Recipient}.
	 *
	 * @param recipient Preconfigured recipient (name is optional).
	 */
	public EmailBuilderOld bounceTo(@Nonnull final Recipient recipient) {
		checkNonEmptyArgument(recipient, "bounceToRecipient");
		this.bounceToRecipient = new Recipient(recipient.getName(), recipient.getAddress(), null);
		return this;
	}

	/**
	 * Sets the {@link #subject}.
	 */
	public EmailBuilderOld subject(@Nonnull final String subject) {
		this.subject = checkNonEmptyArgument(subject, "subject");
		return this;
	}

	/**
	 * Sets the {@link #text}.
	 */
	public EmailBuilderOld text(@Nullable final String text) {
		this.text = text;
		return this;
	}

	/**
	 * Prepends {@link #text}.
	 */
	public EmailBuilderOld prependText(@Nonnull final String text) {
		this.text = text + defaultTo(this.text, "");
		return this;
	}

	/**
	 * Appends {@link #text}.
	 */
	public EmailBuilderOld appendText(@Nonnull final String text) {
		this.text = defaultTo(this.text, "") + text;
		return this;
	}

	/**
	 * Sets the {@link #textHTML}.
	 */
	public EmailBuilderOld textHTML(@Nullable final String textHTML) {
		this.textHTML = textHTML;
		return this;
	}

	/**
	 * Prepends {@link #textHTML}.
	 */
	public EmailBuilderOld prependTextHTML(@Nonnull final String textHTML) {
		this.textHTML = textHTML + defaultTo(this.textHTML, "");
		return this;
	}

	/**
	 * Appends {@link #textHTML}.
	 */
	public EmailBuilderOld appendTextHTML(@Nonnull final String textHTML) {
		this.textHTML = defaultTo(this.textHTML, "") + textHTML;
		return this;
	}

	/**
	 * Adds new {@link Recipient} instances to the list on account of name, address with recipient type {@link Message.RecipientType#TO}.
	 *
	 * @param recipientsToAdd The recipients whose name and address to use
	 * @see #recipients
	 * @see Recipient
	 */
	public EmailBuilderOld to(@Nonnull final Recipient... recipientsToAdd) {
		return to(asList(recipientsToAdd));
	}

	/**
	 * Adds new {@link Recipient} instances to the list on account of name, address with recipient type {@link Message.RecipientType#TO}.
	 *
	 * @param recipientsToAdd The recipients whose name and address to use
	 * @see #recipients
	 * @see Recipient
	 */
	public EmailBuilderOld to(@Nonnull final Collection<Recipient> recipientsToAdd) {
		for (final Recipient recipient : checkNonEmptyArgument(recipientsToAdd, "recipientsToAdd")) {
			recipients.add(new Recipient(recipient.getName(), recipient.getAddress(), Message.RecipientType.TO));
		}
		return this;
	}

	/**
	 * Delegates to {@link #to(String, String)} while omitting the name used for the recipient(s).
	 */
	public EmailBuilderOld to(@Nonnull final String emailAddressList) {
		return to(null, emailAddressList);
	}

	/**
	 * Adds a new {@link Recipient} instances to the list on account of given name, address with recipient type {@link Message.RecipientType#TO}.
	 * List can be comma ',' or semicolon ';' separated.
	 *
	 * @param name             The name of the recipient(s).
	 * @param emailAddressList The emailaddresses of the recipients (will be singular in most use cases).
	 * @see #recipients
	 * @see Recipient
	 */
	public EmailBuilderOld to(@Nullable final String name, @Nonnull final String emailAddressList) {
		checkNonEmptyArgument(emailAddressList, "emailAddressList");
		return addCommaOrSemicolonSeparatedEmailAddresses(name, emailAddressList, Message.RecipientType.TO);
	}

	@Nonnull
	private EmailBuilderOld addCommaOrSemicolonSeparatedEmailAddresses(@Nullable final String name, @Nonnull final String emailAddressList, @Nonnull final Message.RecipientType type) {
		checkNonEmptyArgument(type, "type");
		for (final String emailAddress : extractEmailAddresses(checkNonEmptyArgument(emailAddressList, "emailAddressList"))) {
			recipients.add(Email.interpretRecipientData(name, emailAddress, type));
		}
		return this;
	}

	/**
	 * Adds new {@link Recipient} instances to the list on account of empty name, address with recipient type {@link Message.RecipientType#TO}.
	 *
	 * @param emailAddresses The recipients whose address to use for both name and address
	 * @see #recipients
	 * @see Recipient
	 */
	public EmailBuilderOld to(@Nonnull final String... emailAddresses) {
		for (final String emailAddress : checkNonEmptyArgument(emailAddresses, "emailAddresses")) {
			recipients.add(new Recipient(null, emailAddress, Message.RecipientType.TO));
		}
		return this;
	}

	/**
	 * Adds new {@link Recipient} instances to the list on account of empty name, address with recipient type {@link Message.RecipientType#CC}.
	 *
	 * @param emailAddresses The recipients whose address to use for both name and address
	 * @see #recipients
	 * @see Recipient
	 */
	@SuppressWarnings("QuestionableName")
	public EmailBuilderOld cc(@Nonnull final String... emailAddresses) {
		for (final String emailAddress : checkNonEmptyArgument(emailAddresses, "emailAddresses")) {
			recipients.add(new Recipient(null, emailAddress, Message.RecipientType.CC));
		}
		return this;
	}


	/**
	 * Delegates to {@link #cc(String, String)} while omitting the name for the CC recipient(s).
	 */
	@SuppressWarnings("QuestionableName")
	public EmailBuilderOld cc(@Nonnull final String emailAddressList) {
		return cc(null, emailAddressList);
	}

	/**
	 * Adds a new {@link Recipient} instances to the list on account of empty name, address with recipient type {@link Message.RecipientType#CC}. List can be
	 * comma ',' or semicolon ';' separated.
	 *
	 * @param name             The name of the recipient(s).
	 * @param emailAddressList The recipients whose address to use for both name and address
	 * @see #recipients
	 * @see Recipient
	 */
	@SuppressWarnings("QuestionableName")
	public EmailBuilderOld cc(@Nullable final String name, @Nonnull final String emailAddressList) {
		checkNonEmptyArgument(emailAddressList, "emailAddressList");
		return addCommaOrSemicolonSeparatedEmailAddresses(name, emailAddressList, Message.RecipientType.CC);
	}

	/**
	 * Adds new {@link Recipient} instances to the list on account of name, address with recipient type {@link Message.RecipientType#CC}.
	 *
	 * @param recipientsToAdd The recipients whose name and address to use
	 * @see #recipients
	 * @see Recipient
	 */
	@SuppressWarnings("QuestionableName")
	public EmailBuilderOld cc(@Nonnull final Recipient... recipientsToAdd) {
		for (final Recipient recipient : checkNonEmptyArgument(recipientsToAdd, "recipientsToAdd")) {
			recipients.add(new Recipient(recipient.getName(), recipient.getAddress(), Message.RecipientType.CC));
		}
		return this;
	}

	/**
	 * Adds new {@link Recipient} instances to the list on account of empty name, address with recipient type {@link Message.RecipientType#BCC}.
	 *
	 * @param emailAddresses The recipients whose address to use for both name and address
	 * @see #recipients
	 * @see Recipient
	 */
	public EmailBuilderOld bcc(@Nonnull final String... emailAddresses) {
		for (final String emailAddress : checkNonEmptyArgument(emailAddresses, "emailAddresses")) {
			recipients.add(new Recipient(null, emailAddress, Message.RecipientType.BCC));
		}
		return this;
	}

	/**
	 * Delegates to {@link #bcc(String, String)} while omitting the name for the BCC recipient(s).
	 */
	public EmailBuilderOld bcc(@Nonnull final String emailAddressList) {
		return bcc(null, emailAddressList);
	}

	/**
	 * Adds a new {@link Recipient} instances to the list on account of empty name, address with recipient type {@link Message.RecipientType#BCC}. List can be
	 * comma ',' or semicolon ';' separated.
	 *
	 * @param name             The name of the recipient(s).
	 * @param emailAddressList The recipients whose address to use for both name and address
	 * @see #recipients
	 * @see Recipient
	 */
	public EmailBuilderOld bcc(@Nullable final String name, @Nonnull final String emailAddressList) {
		checkNonEmptyArgument(emailAddressList, "emailAddressList");
		return addCommaOrSemicolonSeparatedEmailAddresses(name, emailAddressList, Message.RecipientType.BCC);
	}

	/**
	 * Adds new {@link Recipient} instances to the list on account of name, address with recipient type {@link Message.RecipientType#BCC}.
	 *
	 * @param recipientsToAdd The recipients whose name and address to use
	 * @see #recipients
	 * @see Recipient
	 */
	public EmailBuilderOld bcc(@Nonnull final Recipient... recipientsToAdd) {
		for (final Recipient recipient : checkNonEmptyArgument(recipientsToAdd, "recipientsToAdd")) {
			recipients.add(new Recipient(recipient.getName(), recipient.getAddress(), Message.RecipientType.BCC));
		}
		return this;
	}

	/**
	 * Adds an embedded image (attachment type) to the email message and generates the necessary {@link DataSource} with the given byte data. Then
	 * delegates to {@link Email#addEmbeddedImage(String, DataSource)}. At this point the datasource is actually a {@link ByteArrayDataSource}.
	 *
	 * @param name     The name of the image as being referred to from the message content body (eg. 'signature').
	 * @param data     The byte data of the image to be embedded.
	 * @param mimetype The content type of the given data (eg. "image/gif" or "image/jpeg").
	 * @see ByteArrayDataSource
	 * @see Email#addEmbeddedImage(String, DataSource)
	 */
	public EmailBuilderOld embedImage(@Nonnull final String name, @Nonnull final byte[] data, @Nonnull final String mimetype) {
		checkNonEmptyArgument(name, "name");
		checkNonEmptyArgument(data, "data");
		checkNonEmptyArgument(mimetype, "mimetype");

		final ByteArrayDataSource dataSource = new ByteArrayDataSource(data, mimetype);
		dataSource.setName(name);
		return embedImage(name, dataSource);
	}

	/**
	 * Delegates to {@link #embedImage(String, DataSource)} for each embedded image.
	 */
	private EmailBuilderOld withEmbeddedImages(@Nonnull final List<AttachmentResource> embeddedImages) {
		for (final AttachmentResource embeddedImage : embeddedImages) {
			embedImage(embeddedImage.getName(), embeddedImage.getDataSource());
		}
		return this;
	}

	/**
	 * Overloaded method which sets an embedded image on account of name and {@link DataSource}.
	 *
	 * @param name      The name of the image as being referred to from the message content body (eg. 'embeddedimage'). If not provided, the name of the given
	 *                  data source is used instead.
	 * @param imagedata The image data.
	 */
	@SuppressWarnings("WeakerAccess")
	public EmailBuilderOld embedImage(@Nullable final String name, @Nonnull final DataSource imagedata) {
		checkNonEmptyArgument(imagedata, "imagedata");
		if (valueNullOrEmpty(name) && valueNullOrEmpty(imagedata.getName())) {
			throw new EmailException(EmailException.NAME_MISSING_FOR_EMBEDDED_IMAGE);
		}
		embeddedImages.add(new AttachmentResource(name, imagedata));
		return this;
	}

	@SuppressWarnings("WeakerAccess")
	public EmailBuilderOld withHeaders(@Nonnull final Map<String, String> headers) {
		this.headers.putAll(headers);
		return this;
	}

	/**
	 * Adds a header to the {@link #headers} list. The value is stored as a <code>String</code>. example: <code>email.addHeader("X-Priority",
	 * 2)</code>
	 *
	 * @param name  The name of the header.
	 * @param value The value of the header, which will be stored using {@link String#valueOf(Object)}.
	 */
	public EmailBuilderOld addHeader(@Nonnull final String name, @Nonnull final Object value) {
		checkNonEmptyArgument(name, "name");
		checkNonEmptyArgument(value, "value");
		headers.put(name, String.valueOf(value));
		return this;
	}

	/**
	 * Adds an attachment to the email message and generates the necessary {@link DataSource} with the given byte data. Then delegates to {@link
	 * #addAttachment(String, DataSource)}. At this point the datasource is actually a {@link ByteArrayDataSource}.
	 *
	 * @param name     The name of the extension (eg. filename including extension).
	 * @param data     The byte data of the attachment.
	 * @param mimetype The content type of the given data (eg. "plain/text", "image/gif" or "application/pdf").
	 * @see ByteArrayDataSource
	 * @see #addAttachment(String, DataSource)
	 */
	public EmailBuilderOld addAttachment(@Nullable final String name, @Nonnull final byte[] data, @Nonnull final String mimetype) {
		checkNonEmptyArgument(data, "data");
		checkNonEmptyArgument(mimetype, "mimetype");
		final ByteArrayDataSource dataSource = new ByteArrayDataSource(data, mimetype);
		dataSource.setName(MiscUtil.encodeText(name));
		addAttachment(MiscUtil.encodeText(name), dataSource);
		return this;
	}

	/**
	 * Overloaded method which sets an attachment on account of name and {@link DataSource}.
	 *
	 * @param name     The name of the attachment (eg. 'filename.ext').
	 * @param filedata The attachment data.
	 */
	public EmailBuilderOld addAttachment(@Nullable final String name, @Nonnull final DataSource filedata) {
		checkNonEmptyArgument(filedata, "filedata");
		attachments.add(new AttachmentResource(MiscUtil.encodeText(name), filedata));
		return this;
	}

	/**
	 * Sets all info needed for DKIM, using a byte array for private key data.
	 */
	public EmailBuilderOld signWithDomainKey(@Nonnull final byte[] dkimPrivateKey, @Nonnull final String signingDomain, @Nonnull final String dkimSelector) {
		this.dkimPrivateKeyInputStream = new ByteArrayInputStream(checkNonEmptyArgument(dkimPrivateKey, "dkimPrivateKey"));
		this.signingDomain = checkNonEmptyArgument(signingDomain, "signingDomain");
		this.dkimSelector = checkNonEmptyArgument(dkimSelector, "dkimSelector");
		return this;
	}

	/**
	 * Sets all info needed for DKIM, using a byte array for private key data.
	 */
	public EmailBuilderOld signWithDomainKey(@Nonnull final String dkimPrivateKey, @Nonnull final String signingDomain, @Nonnull final String dkimSelector) {
		checkNonEmptyArgument(dkimPrivateKey, "dkimPrivateKey");
		this.dkimPrivateKeyInputStream = new ByteArrayInputStream(dkimPrivateKey.getBytes(UTF_8));
		this.signingDomain = checkNonEmptyArgument(signingDomain, "signingDomain");
		this.dkimSelector = checkNonEmptyArgument(dkimSelector, "dkimSelector");
		return this;
	}

	/**
	 * Sets all info needed for DKIM, using a file reference for private key data.
	 */
	public EmailBuilderOld signWithDomainKey(@Nonnull final File dkimPrivateKeyFile, @Nonnull final String signingDomain, @Nonnull final String dkimSelector) {
		this.dkimPrivateKeyFile = checkNonEmptyArgument(dkimPrivateKeyFile, "dkimPrivateKeyFile");
		this.signingDomain = checkNonEmptyArgument(signingDomain, "signingDomain");
		this.dkimSelector = checkNonEmptyArgument(dkimSelector, "dkimSelector");
		return this;
	}

	/**
	 * Sets all info needed for DKIM, using an input stream for private key data.
	 */
	public EmailBuilderOld signWithDomainKey(@Nonnull final InputStream dkimPrivateKeyInputStream, @Nonnull final String signingDomain,
										  @Nonnull final String dkimSelector) {
		this.dkimPrivateKeyInputStream = checkNonEmptyArgument(dkimPrivateKeyInputStream, "dkimPrivateKeyInputStream");
		this.signingDomain = checkNonEmptyArgument(signingDomain, "signingDomain");
		this.dkimSelector = checkNonEmptyArgument(dkimSelector, "dkimSelector");
		return this;
	}

	/**
	 * Indicates that we want to use the NPM flag {@link #dispositionNotificationTo}. The actual address will default to the {@link #replyToRecipient}
	 * first if set or else {@link #fromRecipient}.
	 */
	public EmailBuilderOld withDispositionNotificationTo() {
		this.useDispositionNotificationTo = true;
		this.dispositionNotificationTo = null;
		return this;
	}

	/**
	 * Indicates that we want to use the NPM flag {@link #dispositionNotificationTo} with the given mandatory address.
	 */
	public EmailBuilderOld withDispositionNotificationTo(@Nonnull final String address) {
		this.useDispositionNotificationTo = true;
		this.dispositionNotificationTo = new Recipient(null, checkNonEmptyArgument(address, "dispositionNotificationToAddress"), null);
		return this;
	}

	/**
	 * Indicates that we want to use the NPM flag {@link #dispositionNotificationTo} with the given optional name and mandatory address.
	 */
	public EmailBuilderOld withDispositionNotificationTo(@Nullable final String name, @Nonnull final String address) {
		this.useDispositionNotificationTo = true;
		this.dispositionNotificationTo = new Recipient(name, checkNonEmptyArgument(address, "dispositionNotificationToAddress"), null);
		return this;
	}

	/**
	 * Indicates that we want to use the NPM flag {@link #dispositionNotificationTo} with the given preconfigred {@link Recipient}.
	 */
	public EmailBuilderOld withDispositionNotificationTo(@Nonnull final Recipient recipient) {
		this.useDispositionNotificationTo = true;
		this.dispositionNotificationTo = new Recipient(recipient.getName(), checkNonEmptyArgument(recipient.getAddress(), "dispositionNotificationToAddress"), null);
		return this;
	}

	/**
	 * Indicates that we want to use the flag {@link #returnReceiptTo}. The actual address will default to the {@link #replyToRecipient}
	 * first if set or else {@link #fromRecipient}.
	 */
	public EmailBuilderOld withReturnReceiptTo() {
		this.useReturnReceiptTo = true;
		this.returnReceiptTo = null;
		return this;
	}

	/**
	 * Indicates that we want to use the NPM flag {@link #returnReceiptTo} with the given mandatory address.
	 */
	public EmailBuilderOld withReturnReceiptTo(@Nonnull final String address) {
		this.useReturnReceiptTo = true;
		this.returnReceiptTo = new Recipient(null, checkNonEmptyArgument(address, "returnReceiptToAddress"), null);
		return this;
	}

	/**
	 * Indicates that we want to use the NPM flag {@link #returnReceiptTo} with the given optional name and mandatory address.
	 */
	public EmailBuilderOld withReturnReceiptTo(@Nullable final String name, @Nonnull final String address) {
		this.useReturnReceiptTo = true;
		this.returnReceiptTo = new Recipient(name, checkNonEmptyArgument(address, "returnReceiptToAddress"), null);
		return this;
	}

	/**
	 * Indicates that we want to use the NPM flag {@link #returnReceiptTo} with the preconfigured {@link Recipient}.
	 */
	public EmailBuilderOld withReturnReceiptTo(@Nonnull final Recipient recipient) {
		this.useReturnReceiptTo = true;
		this.returnReceiptTo = new Recipient(recipient.getName(), checkNonEmptyArgument(recipient.getAddress(), "returnReceiptToAddress"), null);
		return this;
	}

	/**
	 * Delegates to {@link #asReplyTo(MimeMessage, boolean, String)} with replyToAll set to <code>false</code> and a default HTML quoting
	 * template.
	 */
	public EmailBuilderOld asReplyTo(@Nonnull final Email email) {
		return asReplyTo(EmailConverter.emailToMimeMessage(email), false, DEFAULT_QUOTING_MARKUP);
	}

	/**
	 * Delegates to {@link #asReplyTo(MimeMessage, boolean, String)} with replyToAll set to <code>true</code> and a default HTML quoting
	 * template.
	 */
	public EmailBuilderOld asReplyToAll(@Nonnull final Email email) {
		return asReplyTo(EmailConverter.emailToMimeMessage(email), true, DEFAULT_QUOTING_MARKUP);
	}

	/**
	 * Delegates to {@link #asReplyTo(MimeMessage, boolean, String)} with replyToAll set to <code>true</code>.
	 *
	 * @see EmailBuilderOld#DEFAULT_QUOTING_MARKUP
	 */
	public EmailBuilderOld asReplyToAll(@Nonnull final Email email, @Nonnull final String customQuotingTemplate) {
		return asReplyTo(EmailConverter.emailToMimeMessage(email), true, customQuotingTemplate);
	}

	/**
	 * Delegates to {@link #asReplyTo(MimeMessage, boolean, String)} with replyToAll set to <code>false</code>.
	 */
	public EmailBuilderOld asReplyTo(@Nonnull final Email email, @Nonnull final String customQuotingTemplate) {
		return asReplyTo(EmailConverter.emailToMimeMessage(email), false, customQuotingTemplate);
	}

	/**
	 * Delegates to {@link #asReplyTo(MimeMessage, boolean, String)} with replyToAll set to <code>false</code> and a default HTML quoting
	 * template.
	 */
	public EmailBuilderOld asReplyTo(@Nonnull final MimeMessage email) {
		return asReplyTo(email, false, DEFAULT_QUOTING_MARKUP);
	}

	/**
	 * Delegates to {@link #asReplyTo(MimeMessage, boolean, String)} with replyToAll set to <code>true</code>.
	 *
	 * @see EmailBuilderOld#DEFAULT_QUOTING_MARKUP
	 */
	public EmailBuilderOld asReplyToAll(@Nonnull final MimeMessage email, @Nonnull final String customQuotingTemplate) {
		return asReplyTo(email, true, customQuotingTemplate);
	}

	/**
	 * Delegates to {@link #asReplyTo(MimeMessage, boolean, String)} with replyToAll set to <code>false</code>.
	 */
	public EmailBuilderOld asReplyTo(@Nonnull final MimeMessage email, @Nonnull final String customQuotingTemplate) {
		return asReplyTo(email, false, customQuotingTemplate);
	}

	/**
	 * Delegates to {@link #asReplyTo(MimeMessage, boolean, String)} with replyToAll set to <code>true</code> and a default HTML quoting
	 * template.
	 *
	 * @see EmailBuilderOld#DEFAULT_QUOTING_MARKUP
	 */
	public EmailBuilderOld asReplyToAll(@Nonnull final MimeMessage email) {
		return asReplyTo(email, true, DEFAULT_QUOTING_MARKUP);
	}

	/**
	 * Primes the email with all subject, headers, originally embedded images and recipients needed for a valid RFC reply.
	 * <p>
	 * <strong>Note:</strong> replaces subject with "Re: &lt;original subject&gt;" (but never nested).<br>
	 * <p>
	 * <strong>Note:</strong> Make sure you set the content before using this API or else the quoted content is lost. Replaces body (text is replaced
	 * with "> text" and HTML is replaced with the provided or default quoting markup.
	 *
	 * @param htmlTemplate A valid HTML that contains the string {@code "%s"}. Be advised that HTML is very limited in emails.
	 * @see <a href="https://javaee.github.io/javamail/FAQ#reply">Official JavaMail FAQ on replying</a>
	 * @see MimeMessage#reply(boolean)
	 */
	public EmailBuilderOld asReplyTo(@Nonnull final MimeMessage emailMessage, final boolean repyToAll, @Nonnull final String htmlTemplate) {
		final MimeMessage replyMessage;
		try {
			replyMessage = (MimeMessage) emailMessage.reply(repyToAll);
			replyMessage.setText("ignore");
			replyMessage.setFrom("ignore@ignore.ignore");
		} catch (final MessagingException e) {
			throw new EmailException("was unable to parse mimemessage to produce a reply for", e);
		}
		
		final Email repliedTo = EmailConverter.mimeMessageToEmail(emailMessage);
		final Email generatedReply = EmailConverter.mimeMessageToEmail(replyMessage);

		return this
				.subject(generatedReply.getSubject())
				.to(generatedReply.getRecipients())
				.text(valueNullOrEmpty(repliedTo.getText()) ? text : text + LINE_START_PATTERN.matcher(repliedTo.getText()).replaceAll("> "))
				.textHTML(valueNullOrEmpty(repliedTo.getTextHTML()) ? textHTML : textHTML + format(htmlTemplate, repliedTo.getTextHTML()))
				.withHeaders(generatedReply.getHeaders())
				.withEmbeddedImages(repliedTo.getEmbeddedImages());
	}
	
	/**
	 * Delegates to {@link #asForwardOf(MimeMessage)}.
	 *
	 * @see EmailConverter#emailToMimeMessage(Email)
	 */
	public EmailBuilderOld asForwardOf(@Nonnull final Email email) {
		return asForwardOf(EmailConverter.emailToMimeMessage(email));
	}
	
	/**
	 * Primes the email to build with proper subject and inline forwarded email needed for a valid RFC forward.
	 * <p>
	 * <strong>Note</strong>: replaces subject with "Fwd: &lt;original subject&gt;" (nesting enabled).
	 * <p>
	 * <strong>Note</strong>: {@code Content-Disposition} will be left empty so the receiving email client can decide how to handle display (most will show
	 * inline, some will show as attachment instead).
	 *
	 * @see <a href="https://javaee.github.io/javamail/FAQ#forward">Official JavaMail FAQ on forwarding</a>
	 * @see <a href="https://blogs.technet.microsoft.com/exchange/2011/04/21/mixed-ing-it-up-multipartmixed-messages-and-you/">More reading
	 * material</a>
	 */
	public EmailBuilderOld asForwardOf(@Nonnull final MimeMessage emailMessage) {
		this.emailToForward = emailMessage;
		return subject("Fwd: " + MimeMessageParser.parseSubject(emailMessage));
	}
}