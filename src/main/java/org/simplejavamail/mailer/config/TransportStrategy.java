package org.simplejavamail.mailer.config;

import javax.mail.Session;
import java.util.Properties;

import static java.lang.String.format;

/**
 * Defines the various types of transport protocols and implements respective properties so that a {@link Session} may be configured using a
 * <code>TransportStrategy</code> implementation.
 *
 * @author Benny Bottema
 * @author Christian Barcenas
 */
public enum TransportStrategy {

	/**
	 * Vanilla SMTP with an insecure STARTTLS upgrade (if supported).
	 * <p>
	 * This {@code TransportStrategy} falls back to plaintext when a mail server does not indicate support for
	 * STARTTLS. Additionally, even if a TLS session is negotiated, <strong>server certificates are not validated in
	 * any way</strong>.
	 * <p>
	 * This {@code TransportStrategy} only offers protection against passive network eavesdroppers when the mail server
	 * indicates support for STARTTLS. Active network attackers can trivially bypass the encryption 1) by tampering with
	 * the STARTTLS indicator, 2) by presenting a self-signed certificate, 3) by presenting a certificate issued by an
	 * untrusted certificate authority; or 4) by presenting a certificate that was issued by a valid certificate
	 * authority to a domain other than the mail server's.
	 * <p>
	 * For proper mail transport encryption, see {@link TransportStrategy#SMTP_SSL} or
	 * {@link TransportStrategy#SMTP_TLS}.
	 * <p>
	 * Implementation notes:
	 * <ul>
	 *     <li>The transport protocol is explicitly set to {@code smtp}.</li>
	 *     <li>Only {@code mail.smtp} properties are set.</li>
	 *     <li>STARTTLS is enabled by setting {@code mail.smtp.starttls.enable} to {@code true}.</li>
	 *     <li>STARTTLS plaintext fallback is enabled by setting {@code mail.smtp.starttls.required} to {@code false}.</li>
	 *     <li>Certificate issuer checks are disabled by setting {@code mail.smtp.ssl.trust} to {@code "*"}.</li>
	 *     <li>Certificate identity checks are disabled by setting {@code mail.smtp.ssl.checkserveridentity} to {@code false}.</li>
     * </ul>
	 */
	SMTP_PLAIN {
		/**
		 * @see TransportStrategy#SMTP_PLAIN
		 */
		@Override
		public Properties generateProperties() {
			final Properties props = super.generateProperties();
			props.put("mail.transport.protocol", "smtp");
			props.put("mail.smtp.starttls.enable", true);
			props.put("mail.smtp.starttls.required", false);
			props.put("mail.smtp.ssl.trust", "*");
			props.put("mail.smtp.ssl.checkserveridentity", false);
			return props;
		}

		/**
		 * @return "mail.smtp.host"
		 */
		@Override
		public String propertyNameHost() {
			return "mail.smtp.host";
		}

		/**
		 * @return "mail.smtp.port"
		 */
		@Override
		public String propertyNamePort() {
			return "mail.smtp.port";
		}

		/**
		 * @return "mail.smtp.username"
		 */
		@Override
		public String propertyNameUsername() {
			return "mail.smtp.username";
		}

		/**
		 * @return "mail.smtp.auth"
		 */
		@Override
		public String propertyNameAuthenticate() {
			return "mail.smtp.auth";
		}
		
		/**
		 * @return "mail.smtp.socks.host"
		 */
		@Override
		public String propertyNameSocksHost() {
			return "mail.smtp.socks.host";
		}
		
		/**
		 * @return "mail.smtp.socks.port"
		 */
		@Override
		public String propertyNameSocksPort() {
			return "mail.smtp.socks.port";
		}
		
		/**
		 * @return "mail.smtp.connectiontimeout"
		 */
		@Override
		public String propertyNameConnectionTimeout() {
			return "mail.smtp.connectiontimeout";
		}
		
		/**
		 * @return "mail.smtp.timeout"
		 */
		@Override
		public String propertyNameTimeout() {
			return "mail.smtp.timeout";
		}
		
		/**
		 * @return "mail.smtp.writetimeout"
		 */
		@Override
		public String propertyNameWriteTimeout() {
			return "mail.smtp.writetimeout";
		}
		
		/**
		 * @return "mail.smtp.from"
		 */
		@Override
		public String propertyNameEnvelopeFrom() {
			return "mail.smtp.from";
		}
		
		/**
		 * @return "mail.smtp.ssl.trust"
		 */
		@Override
		public String propertyNameSSLTrust() {
			return "mail.smtp.ssl.trust";
		}
	},
	/**
	 * SMTP entirely encapsulated by TLS. Commonly known as SMTPS.
	 * <p>
	 * Strict validation of server certificates is enabled. Server certificates must be issued 1) by a certificate
	 * authority in the system trust store; and 2) to a subject matching the identity of the remote SMTP server.
	 * <p>
	 * Implementation notes:
	 * <ul>
	 *     <li>The transport protocol is explicitly set to {@code smtps}.</li>
	 *     <li>Only {@code mail.smtps} properties are set.</li>
	 *     <li>Certificate identity checks are enabled by setting {@code mail.smtp.ssl.checkserveridentity} to {@code true}.</li>
	 *     <li>
	 * {@code mail.smtps.quitwait} is set to {@code false} to get rid of a strange SSLException:
	 * <pre>
	 * javax.mail.MessagingException: Exception reading response;
	 * nested exception is:
	 * 	javax.net.ssl.SSLException: Unsupported record version Unknown-50.49
	 * (..)
	 * </pre>
	 * <p>
	 * <blockquote>The mail is sent but the exception is unwanted. The property <em>quitwait</em> means If set to false, the QUIT command is sent and
	 * the connection is immediately closed. If set to true (the default), causes the transport to wait for the response to the QUIT
	 * command</blockquote><br> <strong>- <a href="http://www.rgagnon.com/javadetails/java-0570.html">source</a></strong>
	 *     </li>
	 * </ul>
	 */
	SMTP_SSL {
		/**
		 * @see TransportStrategy#SMTP_SSL
		 */
		@Override
		public Properties generateProperties() {
			final Properties properties = super.generateProperties();
			properties.put("mail.transport.protocol", "smtps");
			properties.put("mail.smtps.ssl.checkserveridentity", "true");
			properties.put("mail.smtps.quitwait", "false");
			return properties;
		}

		/**
		 * @return "mail.smtps.host"
		 */
		@Override
		public String propertyNameHost() {
			return "mail.smtps.host";
		}

		/**
		 * @return "mail.smtps.port"
		 */
		@Override
		public String propertyNamePort() {
			return "mail.smtps.port";
		}

		/**
		 * @return "mail.smtps.username"
		 */
		@Override
		public String propertyNameUsername() {
			return "mail.smtps.username";
		}

		/**
		 * @return "mail.smtps.auth"
		 */
		@Override
		public String propertyNameAuthenticate() {
			return "mail.smtps.auth";
		}
		
		/**
		 * @return "mail.smtps.socks.host"
		 */
		@Override
		public String propertyNameSocksHost() {
			return "mail.smtps.socks.host";
		}
		
		/**
		 * @return "mail.smtps.socks.port"
		 */
		@Override
		public String propertyNameSocksPort() {
			return "mail.smtps.socks.port";
		}
		
		/**
		 * @return "mail.smtps.connectiontimeout"
		 */
		@Override
		public String propertyNameConnectionTimeout() {
			return "mail.smtps.connectiontimeout";
		}
		
		/**
		 * @return "mail.smtps.timeout"
		 */
		@Override
		public String propertyNameTimeout() {
			return "mail.smtps.timeout";
		}
		
		/**
		 * @return "mail.smtps.writetimeout"
		 */
		@Override
		public String propertyNameWriteTimeout() {
			return "mail.smtps.writetimeout";
		}
		
		/**
		 * @return "mail.smtps.from"
		 */
		@Override
		public String propertyNameEnvelopeFrom() {
			return "mail.smtps.from";
		}
		
		/**
		 * @return "mail.smtps.ssl.trust"
		 */
		@Override
		public String propertyNameSSLTrust() {
			return "mail.smtps.ssl.trust";
		}
	},
	/**
	 * Plaintext SMTP with a mandatory, authenticated STARTTLS upgrade.
	 * <p>
	 * <strong>NOTE: this code is in untested beta state</strong>
	 * <p>
	 * Strict validation of server certificates is enabled. Server certificates must be issued 1) by a certificate
	 * authority in the system trust store; and 2) to a subject matching the identity of the remote SMTP server.
	 * <p>
	 * Implementation notes:
	 * <ul>
	 *     <li>The transport protocol is explicitly set to {@code smtp}.</li>
	 *     <li>Only {@code mail.smtp} properties are set.</li>
	 *     <li>STARTTLS is enabled by setting {@code mail.smtp.starttls.enable} to {@code true}.</li>
	 *     <li>STARTTLS plaintext fallback is disabled by setting {@code mail.smtp.starttls.required} to {@code true}.</li>
	 *     <li>Certificate identity checks are enabled by setting {@code mail.smtp.ssl.checkserveridentity} to {@code true}.</li>
	 * </ul>
	 */
	SMTP_TLS {
		/**
		 * @see TransportStrategy#SMTP_TLS
		 */
		@Override
		public Properties generateProperties() {
			final Properties props = super.generateProperties();
			props.put("mail.transport.protocol", "smtp");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.starttls.required", "true");
			props.put("mail.smtp.ssl.checkserveridentity", "true");
			return props;
		}

		/**
		 * @return "mail.smtp.host"
		 */
		@Override
		public String propertyNameHost() {
			return "mail.smtp.host";
		}

		/**
		 * @return "mail.smtp.port"
		 */
		@Override
		public String propertyNamePort() {
			return "mail.smtp.port";
		}

		/**
		 * @return "mail.smtp.username"
		 */
		@Override
		public String propertyNameUsername() {
			return "mail.smtp.username";
		}

		/**
		 * @return "mail.smtp.auth"
		 */
		@Override
		public String propertyNameAuthenticate() {
			return "mail.smtp.auth";
		}
		
		/**
		 * @return "mail.smtp.socks.host"
		 */
		@Override
		public String propertyNameSocksHost() {
			return "mail.smtp.socks.host";
		}
		
		/**
		 * @return "mail.smtp.socks.port"
		 */
		@Override
		public String propertyNameSocksPort() {
			return "mail.smtp.socks.port";
		}
		
		/**
		 * @return "mail.smtp.connectiontimeout"
		 */
		@Override
		public String propertyNameConnectionTimeout() {
			return "mail.smtp.connectiontimeout";
		}
		
		/**
		 * @return "mail.smtp.timeout"
		 */
		@Override
		public String propertyNameTimeout() {
			return "mail.smtp.timeout";
		}
		
		/**
		 * @return "mail.smtp.writetimeout"
		 */
		@Override
		public String propertyNameWriteTimeout() {
			return "mail.smtp.writetimeout";
		}
		
		/**
		 * @return "mail.smtp.from"
		 */
		@Override
		public String propertyNameEnvelopeFrom() {
			return "mail.smtp.from";
		}
		
		/**
		 * @return "mail.smtp.ssl.trust"
		 */
		@Override
		public String propertyNameSSLTrust() {
			return "mail.smtp.ssl.trust";
		}
	};

	/**
	 * Marker property used to track which {@link TransportStrategy} has been used. This way we can differentiate between preconfigured custom
	 * <code>Session</code> and sessions created by a <code>Mailer</code> instance, without checking each and every property for a specific strategy.
	 * <p>
	 * This is mainly for logging purposes.
	 */
	private static final String TRANSPORT_STRATEGY_MARKER = "simplejavamail.transportstrategy";

	/**
	 * Base implementation that simply returns an empty list of properties and a marker for the specific current strategy.
	 * <p>
	 * Should be overridden by the various strategies where appropriate.
	 *
	 * @return An empty <code>Properties</code> instance.
	 */
	public Properties generateProperties() {
		final Properties properties = new Properties();
		properties.put(TRANSPORT_STRATEGY_MARKER, name());
		return properties;
	}

	public abstract String propertyNameHost();
	public abstract String propertyNamePort();
	public abstract String propertyNameUsername();
	public abstract String propertyNameAuthenticate();
	public abstract String propertyNameSocksHost();
	public abstract String propertyNameSocksPort();
	public abstract String propertyNameConnectionTimeout();
	public abstract String propertyNameWriteTimeout();
	public abstract String propertyNameEnvelopeFrom();
	public abstract String propertyNameSSLTrust();
	public abstract String propertyNameTimeout();
	
	/**
	 * @param session The session to determine the current transport strategy for
	 * @return Which strategy matches the current Session properties.
	 * @see #generateProperties()
	 */
	public static TransportStrategy findStrategyForSession(final Session session) {
		final String transportStrategyMarker = session.getProperty(TRANSPORT_STRATEGY_MARKER);
		if (transportStrategyMarker != null) {
			return TransportStrategy.valueOf(transportStrategyMarker);
		}
		return null;
	}

	public String toString(final Properties properties) {
		return format("session (host: %s, port: %s, username: %s, authenticate: %s, transport: %s)",
				properties.get(propertyNameHost()),
				properties.get(propertyNamePort()),
				properties.get(propertyNameUsername()),
				properties.get(propertyNameAuthenticate()),
				this);
	}
}