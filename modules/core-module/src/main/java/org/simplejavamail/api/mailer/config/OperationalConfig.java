package org.simplejavamail.api.mailer.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.simplejavamail.api.mailer.CustomMailer;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.MailerGenericBuilder;
import org.simplejavamail.api.mailer.MailerRegularBuilder;

import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

/**
 * Contains all the configuration that affect how a {@link Mailer} operates. This includes connection settings such as
 * timeouts, debug mode and which hosts to trust.
 * <p>
 * All of these settings are configured on the {@link MailerGenericBuilder}.
 */
public interface OperationalConfig {
	/**
	 * @see MailerGenericBuilder#async()
	 */
	boolean isAsync();
	
	/**
	 * @see MailerGenericBuilder#withSessionTimeout(Integer)
	 */
	int getSessionTimeout();

	/**
	 * @see MailerGenericBuilder#withThreadPoolSize(Integer)
	 */
	int getThreadPoolSize();

	/**
	 * @see MailerGenericBuilder#withThreadPoolKeepAliveTime(Integer)
	 */
	int getThreadPoolKeepAliveTime();

	/**
	 * @see MailerGenericBuilder#withConnectionPoolCoreSize(Integer)
	 */
	int getConnectionPoolCoreSize();

	/**
	 * @see MailerGenericBuilder#withConnectionPoolMaxSize(Integer)
	 */
	int getConnectionPoolMaxSize();

	/**
	 * @see MailerGenericBuilder#withConnectionPoolClaimTimeoutMillis(Integer)
	 */
	int getConnectionPoolClaimTimeoutMillis();

	/**
	 * @see MailerGenericBuilder#withConnectionPoolExpireAfterMillis(Integer)
	 */
	int getConnectionPoolExpireAfterMillis();

	/**
	 * @see MailerGenericBuilder#withConnectionPoolLoadBalancingStrategy(LoadBalancingStrategy)
	 */
	@NotNull
	LoadBalancingStrategy getConnectionPoolLoadBalancingStrategy();
	
	/**
	 * @see MailerGenericBuilder#withTransportModeLoggingOnly(Boolean)
	 */
	boolean isTransportModeLoggingOnly();

	/**
	 * @see MailerGenericBuilder#withDebugLogging(Boolean)
	 */
	boolean isDebugLogging();

	/**
	 * @see MailerGenericBuilder#disablingAllClientValidation(Boolean)
	 */
	boolean isDisableAllClientValidation();

	/**
	 * @see MailerGenericBuilder#trustingSSLHosts(String...)
	 */
	@NotNull
	List<String> getSslHostsToTrust();

	/**
	 * @see MailerGenericBuilder#trustingAllHosts(boolean)
	 */
	boolean isTrustAllSSLHost();

	/**
	 * @see MailerRegularBuilder#verifyingServerIdentity(boolean)
	 */
	boolean isVerifyingServerIdentity();
	
	/**
	 * @see MailerGenericBuilder#withProperties(Properties)
	 */
	@NotNull
	Properties getProperties();

	/**
	 * @see MailerGenericBuilder#withClusterKey(UUID)
	 */
	@NotNull
	UUID getClusterKey();

	/**
	 * @see MailerGenericBuilder#withExecutorService(ExecutorService)
	 */
	@NotNull
	ExecutorService getExecutorService();

	/**
	 * Indicates whether the executor service was provided by the user or internally. Used to determine if Simple Java Mail should shut down the executor
	 * when the connection pool is shut down.
	 */
	boolean isExecutorServiceIsUserProvided();

	/**
	 * @see MailerGenericBuilder#withCustomMailer(CustomMailer)
	 */
	@Nullable
	CustomMailer getCustomMailer();
}
