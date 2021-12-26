package org.simplejavamail.mailer.internal;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.internal.authenticatedsockssupport.socks5server.AnonymousSocks5Server;
import org.simplejavamail.api.mailer.config.EmailGovernance;
import org.simplejavamail.api.mailer.config.OperationalConfig;
import org.simplejavamail.converter.internal.mimemessage.MimeMessageProducerHelper;
import org.simplejavamail.mailer.internal.util.SessionLogger;
import org.simplejavamail.mailer.internal.util.TransportRunner;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.simplejavamail.converter.EmailConverter.mimeMessageToEML;

/**
 * Separate closure that can be executed directly or from a thread.
 * <p>
 * Note that this Runnable implementation is <strong>not</strong> thread related, it is just to encapsulate the code to
 * be run directly or from a <em>real</em> Runnable.
 */
class SendMailClosure extends AbstractProxyServerSyncingClosure {

	@NotNull private final OperationalConfig operationalConfig;
	@NotNull private final EmailGovernance emailGovernance;
	@NotNull private final Session session;
	@NotNull private final Email email;
	private final boolean asyncForLoggingPurpose;
	private final boolean transportModeLoggingOnly;

	SendMailClosure(@NotNull OperationalConfig operationalConfig, @NotNull EmailGovernance emailGovernance, @NotNull Session session, @NotNull Email email, @Nullable AnonymousSocks5Server proxyServer,
			boolean asyncForLoggingPurpose,
			boolean transportModeLoggingOnly, @NotNull AtomicInteger smtpConnectionCounter) {
		super(smtpConnectionCounter, proxyServer);
		this.operationalConfig = operationalConfig;
		this.emailGovernance = emailGovernance;
		this.session = session;
		this.email = email;
		this.asyncForLoggingPurpose = asyncForLoggingPurpose;
		this.transportModeLoggingOnly = transportModeLoggingOnly;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void executeClosure() {
		LOGGER.trace("sending email...");
		try {
			// fill and send wrapped mime message parts
			final MimeMessage message = MimeMessageProducerHelper.produceMimeMessage(email, session, emailGovernance.getPkcs12ConfigForSmimeSigning());

			SessionLogger.logSession(session, asyncForLoggingPurpose, "mail");
			message.saveChanges(); // some headers and id's will be set for this specific message
			email.internalSetId(message.getMessageID());

			logEmail(message);

			if (transportModeLoggingOnly) {
				LOGGER.info("TRANSPORT_MODE_LOGGING_ONLY: skipping actual sending...");
			} else if (operationalConfig.getCustomMailer() != null) {
				operationalConfig.getCustomMailer().sendMessage(operationalConfig, session, email, message);
			} else {
				TransportRunner.sendMessage(operationalConfig.getClusterKey(), session, message, message.getAllRecipients());
			}
		} catch (final UnsupportedEncodingException e) {
			LOGGER.error("Failed to send email:\n{}", email.getId());
			LOGGER.trace("{}", email);
			throw new MailerException(MailerException.INVALID_ENCODING, e);
		} catch (final MessagingException e) {
			LOGGER.error("Failed to send email:\n{}", email.getId());
			LOGGER.trace("{}", email);
			throw new MailerException(MailerException.GENERIC_ERROR, e);
		} catch (final Exception e) {
			LOGGER.error("Failed to send email:\n{}", email.getId());
			LOGGER.trace("{}", email);
			throw e;
		}
	}

	private void logEmail(final MimeMessage message) {
		if (transportModeLoggingOnly) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("\n\nEmail: {}\n", email);
				LOGGER.info("\n\nMimeMessage: {}\n", mimeMessageToEML(message));
			}
		} else {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("\t\nEmail: {}", email);
				LOGGER.trace("\t\nMimeMessage: {}\n", mimeMessageToEML(message));
			}
		}
	}
}
