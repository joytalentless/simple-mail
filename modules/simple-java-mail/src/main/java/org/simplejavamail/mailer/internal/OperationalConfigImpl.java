package org.simplejavamail.mailer.internal;

import org.simplejavamail.api.mailer.config.LoadBalancingStrategy;
import org.simplejavamail.api.mailer.config.OperationalConfig;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
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
	 * @see org.simplejavamail.api.mailer.MailerGenericBuilder#withClusterKey(UUID)
	 */
	@Nonnull
	private final UUID clusterKey;

	/**
	 * @see org.simplejavamail.api.mailer.MailerGenericBuilder#withConnectionPoolCoreSize(Integer)
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
	 * @see org.simplejavamail.api.mailer.MailerGenericBuilder#withConnectionPoolLoadBalancingStrategy(LoadBalancingStrategy)
	 */
	@Nonnull
	private final LoadBalancingStrategy connectionPoolLoadBalancingStrategy;

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
	 * @see org.simplejavamail.api.mailer.MailerGenericBuilder#verifyingServerIdentity(boolean)
	 */
	private final boolean verifyingServerIdentity;

	/**
	 * @see org.simplejavamail.api.mailer.MailerGenericBuilder#withExecutorService(ExecutorService)
	 */
	@Nonnull
	private final ExecutorService executorService;
	
	OperationalConfigImpl(final boolean async,
			final Properties properties,
			final int sessionTimeout,
			final int threadPoolSize,
			final int threadPoolKeepAliveTime,
			@Nonnull final UUID clusterKey,
			final int connectionPoolCoreSize,
			final int connectionPoolMaxSize,
			final int connectionPoolExpireAfterMillis,
			@Nonnull final LoadBalancingStrategy connectionPoolLoadBalancingStrategy,
			final boolean transportModeLoggingOnly,
			final boolean debugLogging,
			@Nonnull final List<String> sslHostsToTrust,
			final boolean trustAllSSLHost,
			final boolean verifyingServerIdentity,
			@Nonnull final ExecutorService executorService) {
		this.async = async; // can be overridden when calling {@code mailer.send(async = true)}
		this.properties = properties;
		this.sessionTimeout = sessionTimeout;
		this.threadPoolSize = threadPoolSize;
		this.threadPoolKeepAliveTime = threadPoolKeepAliveTime;
		this.clusterKey = clusterKey;
		this.connectionPoolCoreSize = connectionPoolCoreSize;
		this.connectionPoolMaxSize = connectionPoolMaxSize;
		this.connectionPoolExpireAfterMillis = connectionPoolExpireAfterMillis;
		this.connectionPoolLoadBalancingStrategy = connectionPoolLoadBalancingStrategy;
		this.transportModeLoggingOnly = transportModeLoggingOnly;
		this.debugLogging = debugLogging;
		this.sslHostsToTrust = Collections.unmodifiableList(sslHostsToTrust);
		this.trustAllSSLHost = trustAllSSLHost;
		this.verifyingServerIdentity = verifyingServerIdentity;
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
	 * @see OperationalConfig#getConnectionPoolLoadBalancingStrategy()
	 */
	@Nonnull
	@Override
	public LoadBalancingStrategy getConnectionPoolLoadBalancingStrategy() {
		return connectionPoolLoadBalancingStrategy;
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
	 * @see OperationalConfig#isVerifyingServerIdentity()
	 */
	@Override
	public boolean isVerifyingServerIdentity() {
		return verifyingServerIdentity;
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

	@Nonnull
	@Override
	public UUID getClusterKey() {
		return clusterKey;
	}
}