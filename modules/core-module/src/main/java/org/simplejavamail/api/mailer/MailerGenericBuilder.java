package org.simplejavamail.api.mailer;

import org.hazlewood.connor.bottema.emailaddress.EmailAddressCriteria;
import org.simplejavamail.api.internal.clisupport.model.Cli;
import org.simplejavamail.api.internal.clisupport.model.CliBuilderApiType;
import org.simplejavamail.api.mailer.config.TransportStrategy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.mail.Session;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Builder superclass which contains API to take care of all generic Mailer properties unrelated to the SMTP server
 * (host, port, username, password and transport strategy).
 * <p>
 * To start a new Mailer builder, refer to {@link MailerRegularBuilder}.
 */
@Cli.BuilderApiNode(builderApiType = CliBuilderApiType.MAILER)
public interface MailerGenericBuilder<T extends MailerGenericBuilder<?>> {
	/**
	 * The default maximum timeout value for the transport socket is <code>{@value}</code> milliseconds (affects socket connect-,
	 * read- and write timeouts). Can be overridden from a config file or through System variable.
	 */
	int DEFAULT_SESSION_TIMEOUT_MILLIS = 60_000;
	/**
	 * {@value}
	 *
	 * @see #withThreadPoolSize(Integer)
	 */
	int DEFAULT_POOL_SIZE = 10;
	/**
	 * {@value}
	 *
	 * @see #withThreadPoolKeepAliveTime(Integer)
	 */
	int DEFAULT_POOL_KEEP_ALIVE_TIME = 1;
	/**
	 * {@value}
	 *
	 * @see #withConnectionPoolCoreSize(Integer)
	 */
	int DEFAULT_CONNECTIONPOOL_CORE_SIZE = 0;
	/**
	 * {@value}
	 *
	 * @see #withConnectionPoolMaxSize(Integer)
	 */
	int DEFAULT_CONNECTIONPOOL_MAX_SIZE = 4;
	/**
	 * {@value}
	 *
	 * @see #withConnectionPoolExpireAfterMillis(Integer)
	 */
	int DEFAULT_CONNECTIONPOOL_EXPIREAFTER_MILLIS = 5000;
	/**
	 * Default port is <code>{@value}</code>.
	 */
	int DEFAULT_PROXY_PORT = 1080;
	/**
	 * The temporary intermediary SOCKS5 relay server bridge is a server that sits in between JavaMail and the remote proxy.
	 * Default port is <code>{@value}</code>.
	 */
	int DEFAULT_PROXY_BRIDGE_PORT = 1081;
	/**
	 * Defaults to <code>{@value}</code>, sending mails rather than just only logging the mails.
	 */
	boolean DEFAULT_TRANSPORT_MODE_LOGGING_ONLY = false;
	/**
	 * Defaults to <code>{@value}</code>, sending mails rather than just only logging the mails.
	 */
	boolean DEFAULT_JAVAXMAIL_DEBUG = false;

	/**
	 * Changes the default for sending emails and testing server connections to asynchronous (batch mode).
	 * <p>
	 * In case of asynchronous mode, make sure you configure logging to file or inspect the returned {@link AsyncResponse}.
	 * <p>
	 * Note that you can configure a couple of concurrency properties such as thread pool size, keepAlivetime, connection pool size (or even a cluster) etc.
	 *
	 * <p>
	 * <strong>Note:</strong> without configuring a thread pool (see {@link #withExecutorService(ExecutorService)} or
	 * @see #withExecutorService(ExecutorService)
	 * @see #withThreadPoolSize(Integer)
	 * @see #withThreadPoolKeepAliveTime(Integer)
	 * @see #withConnectionPoolCoreSize(Integer)
	 * @see #withConnectionPoolMaxSize(Integer)
	 * @see #withConnectionPoolExpireAfterMillis(Integer)
	 */
	T async();

	/**
	 * Delegates to {@link #withProxyHost(String)} and {@link #withProxyPort(Integer)}.
	 */
	@Cli.ExcludeApi(reason = "API is a subset of a more detailed API")
	T withProxy(@Nullable String proxyHost, @Nullable Integer proxyPort);

	/**
	 * Sets proxy server settings, by delegating to:
	 * <ol>
	 * <li>{@link #withProxyHost(String)}</li>
	 * <li>{@link #withProxyPort(Integer)}</li>
	 * <li>{@link #withProxyUsername(String)}</li>
	 * <li>{@link #withProxyPassword(String)}</li>
	 * </ol>
	 *
	 * @param proxyHost See linked documentation above.
	 * @param proxyPort See linked documentation above.
	 * @param proxyUsername See linked documentation above.
	 * @param proxyPassword See linked documentation above.
	 */
	T withProxy(@Nullable String proxyHost, @Nullable Integer proxyPort, @Nullable String proxyUsername, @Nullable String proxyPassword);

	/**
	 * Sets the optional proxy host, which will override any default that might have been set (through properties file or programmatically).
	 */
	@Cli.ExcludeApi(reason = "API is a subset of a more details API")
	T withProxyHost(@Nullable String proxyHost);

	/**
	 * Sets the proxy port, which will override any default that might have been set (through properties file or programmatically).
	 * <p>
	 * Defaults to {@value DEFAULT_PROXY_PORT} if no custom default property was configured.
	 */
	@Cli.ExcludeApi(reason = "API is a subset of a more details API")
	T withProxyPort(@Nullable Integer proxyPort);

	/**
	 * Sets the optional username to authenticate with the proxy. If set, Simple Java Mail will use its built in proxy bridge to
	 * perform the SOCKS authentication, as the underlying JavaMail framework doesn't support this directly. The execution path
	 * then will be:
	 * <p>
	 * {@code Simple Java Mail client -> JavaMail -> anonymous authentication with local proxy bridge -> full authentication with remote SOCKS proxy -> SMTP server}.
	 */
	@Cli.ExcludeApi(reason = "API is a subset of a more details API")
	T withProxyUsername(@Nullable String proxyUsername);

	/**
	 * Sets the optional password to authenticate with the proxy.
	 * <p>
	 * <strong>Note:</strong> this is only works in combination with the {@value org.simplejavamail.internal.modules.AuthenticatedSocksModule#NAME}.
	 *
	 * @see #withProxyUsername(String)
	 */
	@Cli.ExcludeApi(reason = "API is a subset of a more details API")
	T withProxyPassword(@Nullable String proxyPassword);

	/**
	 * Relevant only when using username authentication with a proxy.
	 * <p>
	 * Overrides the default for the intermediary SOCKS5 relay server bridge, which is a server that sits in between JavaMail and the remote proxy.
	 * <p>
	 * Defaults to {@value DEFAULT_PROXY_BRIDGE_PORT} if no custom default property was configured.
	 * <p>
	 * <strong>Note:</strong> this is only works in combination with the {@value org.simplejavamail.internal.modules.AuthenticatedSocksModule#NAME}.
	 *
	 * @param proxyBridgePort The port to use for the proxy bridging server.
	 *
	 * @see #withProxyUsername(String)
	 */
	T withProxyBridgePort(@Nonnull Integer proxyBridgePort);

	/**
	 * This flag is set on the Session instance through {@link Session#setDebug(boolean)} so that it generates debug information. To get more
	 * information out of the underlying JavaMail framework or out of Simple Java Mail, increase logging config of your chosen logging framework.
	 *
	 * @param debugLogging Enables or disables debug logging with {@code true} or {@code false}.
	 */
	T withDebugLogging(@Nonnull Boolean debugLogging);

	/**
	 * Controls the timeout to use when sending emails (affects socket connect-, read- and write timeouts).
	 * <p>
	 * Will configure a set of properties on the Session instance with the given value, of which the names
	 * depend on the transport strategy:
	 * <ul>
	 *     <li>{@link TransportStrategy#propertyNameConnectionTimeout()}</li>
	 *     <li>{@link TransportStrategy#propertyNameTimeout()}</li>
	 *     <li>{@link TransportStrategy#propertyNameWriteTimeout()}</li>
	 * </ul>
	 *
	 * @param sessionTimeout Duration to use for session timeout.
	 */
	T withSessionTimeout(@Nonnull Integer sessionTimeout);

	/**
	 * Sets the email address validation restrictions when validating and sending emails using the current <code>Mailer</code> instance.
	 * <p>
	 * Defaults to {@link EmailAddressCriteria#RFC_COMPLIANT} if not overridden with a ({@code null}) value.
	 *
	 * @see EmailAddressCriteria
	 * @see #clearEmailAddressCriteria()
	 * @see #resetEmailAddressCriteria()
	 */
	T withEmailAddressCriteria(@Nonnull EnumSet<EmailAddressCriteria> emailAddressCriteria);

	/**
	 * <strong>For advanced use cases.</strong>
	 * <p>
	 * Allows you to fully customize and manage the thread pool, threads and concurrency characteristics when
	 * sending in batch mode.
	 * <p>
	 * Without calling this, by default the {@code NonJvmBlockingThreadPoolExecutor} is used:
	 * <ul>
	 *     <li>with max threads fixed to the given pool size (default is {@value #DEFAULT_POOL_SIZE})</li>
	 *     <li>with keepAliveTime as specified (if greater than zero, core threads will also time out and die off), default is {@value #DEFAULT_POOL_KEEP_ALIVE_TIME}</li>
	 *     <li>A {@link LinkedBlockingQueue}</li>
	 *     <li>The {@code NamedThreadFactory}, which creates named non-daemon threads</li>
	 * </ul>
	 * <p>
	 * <strong>Note:</strong> What makes it NonJvm is that the default keepAliveTime is set to the lowest non-zero value (so 1), so that
	 * any threads will die off as soon as possible, as not to block the JVM from shutting down.
	 * <p>
	 * <strong>Note:</strong> this only works in combination with the {@value org.simplejavamail.internal.modules.BatchModule#NAME}.
	 *
	 * @param executorService A custom executor service (ThreadPoolExecutor), replacing the {@code NonJvmBlockingThreadPoolExecutor}.
	 */
	T withExecutorService(@Nonnull ExecutorService executorService);

	/**
	 * Sets max thread pool size to the given size (default is {@value #DEFAULT_POOL_SIZE}).
	 * <p>
	 * <strong>Note:</strong> this is only used in combination with the {@value org.simplejavamail.internal.modules.BatchModule#NAME}.
	 *
	 * @param threadPoolSize See main description.
	 *
	 * @see #resetThreadPoolSize()
	 * @see #withThreadPoolSize(Integer)
	 */
	T withThreadPoolSize(@Nonnull Integer threadPoolSize);

	/**
	 * When set to a non-zero value (milliseconds), this keepAlivetime is applied to <em>both</em> core and extra threads. This is so that
	 * these threads can never block the JVM from exiting once they finish their task. This is different from daemon threads,
	 * which are abandonded without waiting for them to finish the tasks.
	 * <p>
	 * When set to zero, this keepAliveTime is applied only to extra threads, not core threads. This is the classic executor
	 * behavior, but this blocks the JVM from exiting.
	 * <p>
	 * Defaults to {@value #DEFAULT_POOL_KEEP_ALIVE_TIME}ms.
	 * <p>
	 * <strong>Note:</strong> this is only used in combination with the {@value org.simplejavamail.internal.modules.BatchModule#NAME}.
	 *
	 * @param threadPoolKeepAliveTime Value in milliseconds. See main description for details.
	 *
	 * @see #resetThreadPoolKeepAliveTime()
	 */
	T withThreadPoolKeepAliveTime(@Nonnull Integer threadPoolKeepAliveTime);

	/**
	 * By defining a clusterKey, you can form clusters where other {@link Mailer} instances define
	 * individual connection pools within the same cluster.
	 * <p>
	 * By default a cluster key is uniquely generated, so a new cluster is always generated for a single Mailer,
	 * thus effectively nothing is clustered.
	 *
	 * @see <a href="FIXME">Clustering with Simple Java Mail</a>
	 */
	T withClusterKey(@Nonnull UUID clusterKey);

	/**
	 * Configures the connection pool's core size (default {@value DEFAULT_CONNECTIONPOOL_CORE_SIZE}), which means the SMTP connection pool will keep X connections open at all times until shut down.
	 * Note that this also means that if you configure an auto-expiry timeout, these connections die off and new ones are created immediately to maintain core size.
	 * <p>
	 * <strong>Note:</strong> this is only used in combination with the {@value org.simplejavamail.internal.modules.BatchModule#NAME}.
	 */
	T withConnectionPoolCoreSize(@Nonnull Integer connectionPoolCoreSize);

	/**
	 * Configured the connection pool's max size (default {@value DEFAULT_CONNECTIONPOOL_MAX_SIZE}) in case of high thread contention. Note that this determines how many connections can
	 * be open at any one time to a single server. Make sure
	 * your
	 * server can handle load coming from all connections.
	 * <p>
	 * There's no point having hundred concurrent connections if it degrades your server's performance.
	 * <p>
	 * In addition, if your server makes connections wait, it means threads will be waiting on the {@link javax.mail.Transport} instance to start their work load, instead of threads being blocked
	 * on a <em>claim</em> for an available {@code Transport} instance. In other words: by having an oversized connection pool, you inadvertently bypass the blocking claim mechanism of the
	 * connection pool and wait on the Transport directly instead.
	 * <p>
	 * <strong>Note:</strong> this is only used in combination with the {@value org.simplejavamail.internal.modules.BatchModule#NAME}.
	 */
	T withConnectionPoolMaxSize(@Nonnull Integer connectionPoolMaxSize);

	/**
	 * If {@code >0}, configures the connection pool to automatically close connections after some milliseconds (default {@value DEFAULT_CONNECTIONPOOL_EXPIREAFTER_MILLIS}) since last usage.
	 * <p>
	 * Note that if you combine this with {@link #withConnectionPoolCoreSize(Integer)} also {@code >0} (default is {@value DEFAULT_CONNECTIONPOOL_CORE_SIZE}), connections will keep
	 * closing and openings to keep core pool populated until shut down.
	 * <p>
	 * <strong>Note:</strong> this is only used in combination with the {@value org.simplejavamail.internal.modules.BatchModule#NAME}.
	 */
	T withConnectionPoolExpireAfterMillis(@Nonnull Integer connectionPoolExpireAfterMillis);

	/**
	 * Determines whether at the very last moment an email is sent out using JavaMail's native API or whether the email is simply only logged.
	 *
	 * @param transportModeLoggingOnly Flag {@code true} or {@code false} that enables or disables logging only mode when sending emails.
	 *
	 * @see #resetTransportModeLoggingOnly()
	 */
	T withTransportModeLoggingOnly(@Nonnull Boolean transportModeLoggingOnly);

	/**
	 * Configures the new session to only accept server certificates issued to one of the provided hostnames, <strong>and disables certificate issuer
	 * validation.</strong>
	 * <p>
	 * Passing an empty list resets the current session's trust behavior to the default, and is equivalent to never calling this method in the first
	 * place.
	 * <p>
	 * <strong>Security warning:</strong> Any certificate matching any of the provided host names will be accepted, regardless of the certificate
	 * issuer; attackers can abuse this behavior by serving a matching self-signed certificate during a man-in-the-middle attack.
	 * <p>
	 * This method sets the property {@code mail.smtp.ssl.trust} to a space-separated list of the provided {@code hosts}. If the provided list is
	 * empty, {@code mail.smtp.ssl.trust} is unset.
	 *
	 * @see <a href="https://javaee.github.io/javamail/docs/api/com/sun/mail/smtp/package-summary.html#mail.smtp.ssl.trust"><code>mail.smtp.ssl.trust</code></a>
	 * @see #trustingAllHosts(boolean)
	 */
	T trustingSSLHosts(String... sslHostsToTrust);

	/**
	 * Configures the current session to trust all hosts and don't validate any SSL keys. The property "mail.smtp(s).ssl.trust" is set to "*".
	 *
	 * @param trustAllHosts Flag {@code true} or {@code false} that enables or disables trusting of <strong>all</strong> hosts.
	 *
	 * @see <a href="https://javamail.java.net/nonav/docs/api/com/sun/mail/smtp/package-summary.html#mail.smtp.ssl.trust">mail.smtp.ssl.trust</a>
	 * @see #trustingSSLHosts(String...)
	 */
	T trustingAllHosts(boolean trustAllHosts);

	/**
	 * Adds the given properties to the total list applied to the {@link Session} when building a mailer.
	 *
	 * @see #withProperties(Map)
	 * @see #withProperty(String, Object)
	 * @see #clearProperties()
	 */
	T withProperties(@Nonnull Properties properties);

	/**
	 * @see #withProperties(Properties)
	 * @see #clearProperties()
	 */
	T withProperties(@Nonnull Map<String, String> properties);

	/**
	 * Sets property or removes it if the provided value is <code>null</code>. If provided, the value is always converted <code>toString()</code>.
	 *
	 * @param propertyName  The name of the property that wil be set on the internal Session object.
	 * @param propertyValue The text value of the property that wil be set on the internal Session object.
	 *
	 * @see #withProperties(Properties)
	 * @see #clearProperties()
	 */
	T withProperty(@Nonnull String propertyName, @Nullable Object propertyValue);

	/**
	 * Resets session time to its default ({@value DEFAULT_SESSION_TIMEOUT_MILLIS}).
	 *
	 * @see #withSessionTimeout(Integer)
	 */
	T resetSessionTimeout();

	/**
	 * Resets emailAddressCriteria to {@link EmailAddressCriteria#RFC_COMPLIANT}.
	 *
	 * @see #withEmailAddressCriteria(EnumSet)
	 * @see #clearEmailAddressCriteria()
	 */
	T resetEmailAddressCriteria();

	/**
	 * Resets the executor services to be used back to the default, created by the Batch module if loaded, or else
	 * {@link Executors#newSingleThreadExecutor()}.
	 * <p>
	 * <strong>Note:</strong> this is only used in combination with the {@value org.simplejavamail.internal.modules.BatchModule#NAME}.
	 *
	 * @see #withExecutorService(ExecutorService)
	 * @see
	 * <a href="https://javadoc.io/page/org.simplejavamail/simple-java-mail/latest/org/simplejavamail/internal/batchsupport/concurrent/NonJvmBlockingThreadPoolExecutor.html">Batch module's NonJvmBlockingThreadPoolExecutor</a>
	 */
	T resetExecutorService();

	/**
	 * Resets max thread pool size to its default of {@value #DEFAULT_POOL_SIZE}.
	 * <p>
	 * <strong>Note:</strong> this is only used in combination with the {@value org.simplejavamail.internal.modules.BatchModule#NAME}.
	 *
	 * @see #withThreadPoolSize(Integer)
	 */
	T resetThreadPoolSize();

	/**
	 * Resets thread pool keepAliveTime to its default ({@value #DEFAULT_POOL_KEEP_ALIVE_TIME}).
	 * <p>
	 * <strong>Note:</strong> this is only used in combination with the {@value org.simplejavamail.internal.modules.BatchModule#NAME}.
	 *
	 * @see #withThreadPoolKeepAliveTime(Integer)
	 */
	T resetThreadPoolKeepAliveTime();

	/**
	 * Reset the cluster key to empty, so it will be generated uniquely, avoiding clustering with any other {@link Mailer}.
	 */
	T resetClusterKey();

	/**
	 * Resets connection pool core size to its default ({@value #DEFAULT_CONNECTIONPOOL_CORE_SIZE}).
	 * <p>
	 * <strong>Note:</strong> this is only used in combination with the {@value org.simplejavamail.internal.modules.BatchModule#NAME}.
	 *
	 * @see #withConnectionPoolCoreSize(Integer)
	 */
	T resetConnectionPoolCoreSize();

	/**
	 * Resets connection pool max size to its default ({@value #DEFAULT_CONNECTIONPOOL_MAX_SIZE}).
	 * <p>
	 * <strong>Note:</strong> this is only used in combination with the {@value org.simplejavamail.internal.modules.BatchModule#NAME}.
	 *
	 * @see #withConnectionPoolMaxSize(Integer)
	 */
	T resetConnectionPoolMaxSize();

	/**
	 * Resets connection pool expire-after-milliseconds property to its default ({@value #DEFAULT_CONNECTIONPOOL_EXPIREAFTER_MILLIS}).
	 * <p>
	 * <strong>Note:</strong> this is only used in combination with the {@value org.simplejavamail.internal.modules.BatchModule#NAME}.
	 *
	 * @see #withConnectionPoolExpireAfterMillis(Integer)
	 */
	T resetConnectionPoolExpireAfterMillis();

	/**
	 * Resets transportModeLoggingOnly to {@value #DEFAULT_TRANSPORT_MODE_LOGGING_ONLY}.
	 *
	 * @see #withTransportModeLoggingOnly(Boolean)
	 */
	T resetTransportModeLoggingOnly();

	/**
	 * Empties all proxy configuration.
	 */
	T clearProxy();

	/**
	 * Removes all email address criteria, meaning validation won't take place.
	 *
	 * @see #withEmailAddressCriteria(EnumSet)
	 * @see #resetEmailAddressCriteria()
	 */
	T clearEmailAddressCriteria();

	/**
	 * Removes all trusted hosts from the list.
	 *
	 * @see #trustingSSLHosts(String...)
	 */
	T clearTrustedSSLHosts();

	/**
	 * Removes all properties.
	 *
	 * @see #withProperties(Properties)
	 */
	T clearProperties();

	@Cli.ExcludeApi(reason = "This API is specifically for Java use")
	Mailer buildMailer();

	/**
	 * @see #async()
	 */
	boolean isAsync();

	/**
	 * @see #withProxyHost(String)
	 */
	@Nullable
	String getProxyHost();

	/**
	 * @see #withProxyPort(Integer)
	 */
	@Nullable
	Integer getProxyPort();

	/**
	 * @see #withProxyUsername(String)
	 */
	@Nullable
	String getProxyUsername();

	/**
	 * @see #withProxyPassword(String)
	 */
	@Nullable
	String getProxyPassword();

	/**
	 * @see #withProxyBridgePort(Integer)
	 */
	@Nullable
	Integer getProxyBridgePort();

	/**
	 * @see #withDebugLogging(Boolean)
	 */
	boolean isDebugLogging();

	/**
	 * @see #withSessionTimeout(Integer)
	 */
	@Nullable
	Integer getSessionTimeout();

	/**
	 * @see #withEmailAddressCriteria(EnumSet)
	 */
	@Nullable
	EnumSet<EmailAddressCriteria> getEmailAddressCriteria();

	/**
	 * @see #withExecutorService(ExecutorService)
	 */
	@Nullable
	ExecutorService getExecutorService();

	/**
	 * @see #withThreadPoolSize(Integer)
	 */
	@Nonnull
	Integer getThreadPoolSize();

	/**
	 * @see #withThreadPoolKeepAliveTime(Integer)
	 */
	@Nonnull
	Integer getThreadPoolKeepAliveTime();

	/**
	 * @see #withClusterKey(UUID)
	 */
	@Nonnull
	UUID getClusterKey();

	/**
	 * @see #withConnectionPoolCoreSize(Integer)
	 */
	@Nonnull
	Integer getConnectionPoolCoreSize();

	/**
	 * @see #withConnectionPoolMaxSize(Integer)
	 */
	@Nonnull
	Integer getConnectionPoolMaxSize();

	/**
	 * @see #withConnectionPoolExpireAfterMillis(Integer)
	 */
	@Nonnull
	Integer getConnectionPoolExpireAfterMillis();

	/**
	 * @see #trustingSSLHosts(String...)
	 */
	@Nullable
	List<String> getSslHostsToTrust();

	/**
	 * @see #trustingAllHosts(boolean)
	 */
	boolean isTrustAllSSLHost();

	/**
	 * @see #withTransportModeLoggingOnly(Boolean)
	 */
	boolean isTransportModeLoggingOnly();

	/**
	 * @see #withProperties(Properties)
	 */
	@Nullable
	Properties getProperties();
}