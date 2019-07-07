package org.simplejavamail.mailer.internal;

import org.simplejavamail.api.mailer.config.OperationalConfig;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

/**
 * @see OperationalConfig
 */
// FIXME Lombok, especially builder pattern
class OperationalConfigImpl implements OperationalConfig {
	/**
	 * @see org.simplejavamail.api.mailer.MailerGenericBuilder#withSessionTimeout(Integer)
	 */
	private final int sessionTimeout;
	
	/**
	 * Can be overridden when calling {@code mailer.send(async = true)}.
	 *
	 * @see org.simplejavamail.api.mailer.MailerGenericBuilder#async()
	 */
	private final boolean async;
	/**
	 * @see org.simplejavamail.api.mailer.MailerGenericBuilder#withProperties(Properties)
	 */
	private final Properties properties;

	/**
	 * @see org.simplejavamail.api.mailer.MailerGenericBuilder#withThreadPoolSize(Integer)
	 */
	private final int threadPoolSize;

	/**
	 * @see org.simplejavamail.api.mailer.MailerGenericBuilder#withThreadPoolKeepAliveTime(Integer)
	 */
	private final int threadPoolKeepAliveTime;

	/**
	 * @see MailerRegularBuilder#withConnectionPoolCoreSize(Integer)
	 */
	private final int connectionPoolCoreSize;

	/**
	 * @see org.simplejavamail.api.mailer.MailerGenericBuilder#withConnectionPoolMaxSize(Integer)
	 */
	private final int connectionPoolMaxSize;

	/**
	 * @see org.simplejavamail.api.mailer.MailerGenericBuilder#withConnectionPoolExpireAfterMillis(Integer)
	 */
	private final int connectionPoolExpireAfterMillis;
	
	/**
	 * @see org.simplejavamail.api.mailer.MailerGenericBuilder#withTransportModeLoggingOnly(Boolean)
	 */
	private final boolean transportModeLoggingOnly;
	
	/**
	 * @see org.simplejavamail.api.mailer.MailerGenericBuilder#withDebugLogging(Boolean)
	 */
	private final boolean debugLogging;
	
	/**
	 * @see org.simplejavamail.api.mailer.MailerGenericBuilder#trustingSSLHosts(String...)
	 */
	@Nonnull
	private final List<String> sslHostsToTrust;

	/**
	 * @see org.simplejavamail.api.mailer.MailerGenericBuilder#trustingAllHosts(boolean)
	 */
	private final boolean trustAllSSLHost;

	/**
	 * @see org.simplejavamail.api.mailer.MailerGenericBuilder#withExecutorService(ExecutorService)
	 */
	@Nonnull
	private final ExecutorService executorService;
	
	OperationalConfigImpl(final boolean async, Properties properties, int sessionTimeout, int threadPoolSize, int threadPoolKeepAliveTime, int connectionPoolCoreSize, int connectionPoolMaxSize, int connectionPoolExpireAfterMillis, boolean transportModeLoggingOnly, boolean debugLogging, @Nonnull List<String> sslHostsToTrust, boolean trustAllSSLHost, @Nonnull final ExecutorService executorService) {
		this.async = async; // can be overridden when calling {@code mailer.send(async = true)}
		this.properties = properties;
		this.sessionTimeout = sessionTimeout;
		this.threadPoolSize = threadPoolSize;
		this.threadPoolKeepAliveTime = threadPoolKeepAliveTime;
		this.connectionPoolCoreSize = connectionPoolCoreSize;
		this.connectionPoolMaxSize = connectionPoolMaxSize;
		this.connectionPoolExpireAfterMillis = connectionPoolExpireAfterMillis;
		this.transportModeLoggingOnly = transportModeLoggingOnly;
		this.debugLogging = debugLogging;
		this.sslHostsToTrust = Collections.unmodifiableList(sslHostsToTrust);
		this.trustAllSSLHost = trustAllSSLHost;
		this.executorService = executorService;
	}

	/**
	 * @see OperationalConfig#isAsync()
	 */
	@Override
	public boolean isAsync() {
		return async;
	}
	
	/**
	 * @see OperationalConfig#getSessionTimeout()
	 */
	@Override
	public int getSessionTimeout() {
		return sessionTimeout;
	}

	/**
	 * @see OperationalConfig#getThreadPoolSize()
	 */
	@Override
	public int getThreadPoolSize() {
		return threadPoolSize;
	}

	/**
	 * @see OperationalConfig#getThreadPoolKeepAliveTime()
	 */
	@Override
	public int getThreadPoolKeepAliveTime() {
		return threadPoolKeepAliveTime;
	}

	/**
	 * @see OperationalConfig#getConnectionPoolCoreSize()
	 */
	@Override
	public int getConnectionPoolCoreSize() {
		return connectionPoolCoreSize;
	}

	/**
	 * @see OperationalConfig#getConnectionPoolMaxSize()
	 */
	@Override
	public int getConnectionPoolMaxSize() {
		return connectionPoolMaxSize;
	}

	/**
	 * @see OperationalConfig#getConnectionPoolExpireAfterMillis()
	 */
	@Override
	public int getConnectionPoolExpireAfterMillis() {
		return connectionPoolExpireAfterMillis;
	}
	
	/**
	 * @see OperationalConfig#isTransportModeLoggingOnly()
	 */
	@Override
	public boolean isTransportModeLoggingOnly() {
		return transportModeLoggingOnly;
	}
	
	/**
	 * @see OperationalConfig#isDebugLogging()
	 */
	@Override
	public boolean isDebugLogging() {
		return debugLogging;
	}
	
	/**
	 * @see OperationalConfig#getSslHostsToTrust()
	 */
	@Nonnull
	@Override
	public List<String> getSslHostsToTrust() {
		return sslHostsToTrust;
	}
	
	/**
	 * @see OperationalConfig#isTrustAllSSLHost()
	 */
	@Override
	public boolean isTrustAllSSLHost() {
		return trustAllSSLHost;
	}
	
	/**
	 * @see OperationalConfig#getProperties()
	 */
	@Nonnull
	@Override
	public Properties getProperties() {
		return properties;
	}

	@Nonnull
	@Override
	public ExecutorService getExecutorService() {
		return executorService;
	}
}