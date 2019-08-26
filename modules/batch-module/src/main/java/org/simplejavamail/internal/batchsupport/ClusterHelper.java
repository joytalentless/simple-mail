package org.simplejavamail.internal.batchsupport;

import org.bbottema.clusteredobjectpool.core.ClusterConfig;
import org.bbottema.clusteredobjectpool.core.api.LoadBalancingStrategy;
import org.bbottema.clusteredobjectpool.cyclingstrategies.RandomAccessLoadBalancing;
import org.bbottema.clusteredobjectpool.cyclingstrategies.RoundRobinLoadBalancing;
import org.bbottema.genericobjectpool.expirypolicies.TimeoutSinceLastAllocationExpirationPolicy;
import org.simplejavamail.api.mailer.config.OperationalConfig;
import org.simplejavamail.smtpconnectionpool.SmtpClusterConfig;

import javax.annotation.Nonnull;
import javax.mail.Transport;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.simplejavamail.api.mailer.config.LoadBalancingStrategy.ROUND_ROBIN;

final class ClusterHelper {
	private ClusterHelper() {
		// utility class
	}

	@Nonnull
	static SmtpClusterConfig configureSmtpClusterConfig(@Nonnull final OperationalConfig operationalConfig) {
		SmtpClusterConfig smtpClusterConfig = new SmtpClusterConfig();
		smtpClusterConfig.getConfigBuilder()
				.defaultCorePoolSize(operationalConfig.getConnectionPoolCoreSize())
				.defaultMaxPoolSize(operationalConfig.getConnectionPoolMaxSize())
				.loadBalancingStrategy(operationalConfig.getConnectionPoolLoadBalancingStrategy() == ROUND_ROBIN
						? new RoundRobinLoadBalancing()
						: new RandomAccessLoadBalancing())
				.defaultExpirationPolicy(new TimeoutSinceLastAllocationExpirationPolicy<Transport>(operationalConfig.getConnectionPoolExpireAfterMillis(), MILLISECONDS));
		return smtpClusterConfig;
	}

	static boolean compareClusterConfig(@Nonnull final OperationalConfig operationalConfig, final ClusterConfig config) {
		return config.getDefaultCorePoolSize() != operationalConfig.getConnectionPoolCoreSize() ||
				config.getDefaultMaxPoolSize() != operationalConfig.getConnectionPoolCoreSize() ||
				config.getLoadBalancingStrategy().getClass() != determineLoadBalancingStrategy(operationalConfig).getClass() ||
				!config.getDefaultExpirationPolicy().equals(new TimeoutSinceLastAllocationExpirationPolicy<Transport>(operationalConfig.getConnectionPoolExpireAfterMillis(), MILLISECONDS));
	}

	@Nonnull
	private static LoadBalancingStrategy determineLoadBalancingStrategy(@Nonnull final OperationalConfig operationalConfig) {
		return operationalConfig.getConnectionPoolLoadBalancingStrategy() == ROUND_ROBIN
				? new RoundRobinLoadBalancing()
				: new RandomAccessLoadBalancing();
	}
}
