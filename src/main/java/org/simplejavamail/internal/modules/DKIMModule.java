package org.simplejavamail.internal.modules;

import org.simplejavamail.converter.internal.mimemessage.MimeMessageHelper;
import org.simplejavamail.email.Email;

import javax.mail.internet.MimeMessage;

/**
 * This interface only serves to hide the DKIM implementation behind an easy-to-load-with-reflection class.
 */
public interface DKIMModule {
	/**
	 * @see MimeMessageHelper#signMessageWithDKIM(MimeMessage, Email)
	 */
	MimeMessage signMessageWithDKIM(MimeMessage messageToSign, Email emailContainingSigningDetails);
}
