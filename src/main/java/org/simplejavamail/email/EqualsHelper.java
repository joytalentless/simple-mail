package org.simplejavamail.email;

import javax.activation.DataSource;
import java.util.List;

import static org.simplejavamail.internal.util.MiscUtil.normalizeNewlines;

/**
 * Util class to get rid of some boilerplate code in the core classes. The equals code was needed to analyze junit test errors.
 * <p>
 * Initial equals code generated by IntelliJ, expanded to manually compare objects that don't override {@link Object#equals(Object)} (Recipient and DataSource
 * implementations).
 */
@SuppressWarnings("SimplifiableIfStatement")
final class EqualsHelper {

	public static boolean equalsEmail(final Email email1, final Email email2) {
		if (email1.getFromRecipient() != null ? !isEqualRecipient(email1.getFromRecipient(), email2.getFromRecipient()) : email2.getFromRecipient() != null) {
			return false;
		}
		if (email1.getReplyToRecipient() != null ? !isEqualRecipient(email1.getReplyToRecipient(), email2.getReplyToRecipient()) :
				email2.getReplyToRecipient() != null) {
			return false;
		}
		if (email1.getBounceToRecipient() != null ? !isEqualRecipient(email1.getBounceToRecipient(), email2.getBounceToRecipient()) :
				email2.getBounceToRecipient() != null) {
			return false;
		}
		if (email1.getPlainText() != null ? !email1.getPlainText().equals(email2.getPlainText()) : email2.getPlainText() != null) {
			return false;
		}
		if (email1.getCalendarText() != null ? !email1.getCalendarText().equals(email2.getCalendarText()) : email2.getCalendarText() != null) {
			return false;
		}
		if (email1.getCalendarMethod() != null ? !email1.getCalendarMethod().equals(email2.getCalendarMethod()) : email2.getCalendarMethod() != null) {
			return false;
		}
		if (email1.getEmailToForward() != null ? email2.getEmailToForward() == null : email2.getEmailToForward() != null) {
			return false;
		}
		if (email1.getHTMLText() != null ? !normalizeNewlines(email1.getHTMLText()).equals(normalizeNewlines(email2.getHTMLText())) : email2.getHTMLText() != null) {
			return false;
		}
		if (email1.getSubject() != null ? !email1.getSubject().equals(email2.getSubject()) : email2.getSubject() != null) {
			return false;
		}

		if (!isEqualRecipientList(email1.getRecipients(), email2.getRecipients())) {
			return false;
		}
		if (!email1.getEmbeddedImages().containsAll(email2.getEmbeddedImages())) {
			return false;
		}
		if (!email1.getAttachments().containsAll(email2.getAttachments())) {
			return false;
		}
		if (!email1.getHeaders().equals(email2.getHeaders())) {
			return false;
		}
		if (email1.isUseDispositionNotificationTo() != email2.isUseDispositionNotificationTo()) {
			return false;
		}
		if (email1.isUseReturnReceiptTo() != email2.isUseReturnReceiptTo()) {
			return false;
		}
		if (email1.getDispositionNotificationTo() != null ? !email1.getDispositionNotificationTo().equals(email2.getDispositionNotificationTo()) : email2.getDispositionNotificationTo() != null) {
			return false;
		}
		return email1.getReturnReceiptTo() != null ? email1.getReturnReceiptTo().equals(email2.getReturnReceiptTo()) : email2.getReturnReceiptTo() == null;
	}

	private static boolean isEqualRecipientList(final List<Recipient> recipients, final List<Recipient> otherRecipients) {
		if (recipients.size() != otherRecipients.size()) {
			return false;
		}
		for (final Recipient otherRecipient : otherRecipients) {
			if (!containsRecipient(recipients, otherRecipient)) {
				return false;
			}
		}
		return true;
	}

	private static boolean containsRecipient(final List<Recipient> recipients, final Recipient otherRecipient) {
		for (final Recipient recipient : recipients) {
			if (isEqualRecipient(recipient, otherRecipient)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isEqualRecipient(final Recipient recipient, final Recipient otherRecipient) {
		final String name = recipient != null ? recipient.getName() : null;
		final String otherName = otherRecipient != null ? otherRecipient.getName() : null;
		if (name != null ? !name.equals(otherName) : otherName != null) {
			return false;
		}
		assert otherRecipient != null;
		assert recipient != null;
		if (!recipient.getAddress().equals(otherRecipient.getAddress())) {
			return false;
		}
		return recipient.getType() != null ? recipient.getType().equals(otherRecipient.getType()) : otherRecipient.getType() == null;
	}

	public static boolean equalsAttachmentResource(final AttachmentResource resource1, final AttachmentResource resource2) {
		if (resource1.getName() != null ? !resource1.getName().equals(resource2.getName()) : resource2.getName() != null) {
			return false;
		}
		return resource1.getDataSource() != null ? isEqualDataSource(resource1.getDataSource(), resource2.getDataSource()) : resource2.getDataSource() == null;
	}

	private static boolean isEqualDataSource(final DataSource resource1, final DataSource resource2) {
		if (resource1.getName() != null ? !resource1.getName().equals(resource2.getName()) : resource2.getName() != null) {
			return false;
		}
		return resource1.getContentType() != null ? resource1.getContentType().equals(resource2.getContentType()) : resource2.getContentType() == null;
	}
}
