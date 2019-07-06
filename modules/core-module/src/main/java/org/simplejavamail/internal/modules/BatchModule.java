package org.simplejavamail.internal.modules;

import org.simplejavamail.api.internal.batchsupport.LifecycleDelegatingTransport;
import org.simplejavamail.api.mailer.AsyncResponse;

import javax.annotation.Nonnull;
import javax.mail.Session;
import javax.mail.Transport;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * This interface only serves to hide the Batch implementation behind an easy-to-load-with-reflection class.
 */
public interface BatchModule {

	String NAME = "Advanced batch processing module";

	/**
	 * Executes using a single-execution ExecutorService, which shutdown immediately after the thread finishes.
	 *
	 * @see java.util.concurrent.Executors#newSingleThreadExecutor()
	 */
	@Nonnull
	AsyncResponse executeAsync(@Nonnull String processName, @Nonnull Runnable operation);

	/**
	 * Executes using a the given ExecutorService, which is left running after the thread finishes running.
	 */
	@Nonnull
	AsyncResponse executeAsync(@Nonnull ExecutorService executorService, @Nonnull String processName, @Nonnull Runnable operation);

	/**
	 * @return A NonJvmBlockingThreadPoolExecutor instance that by default doesn't block the JVM from exiting
	 * and produces properly named thread.
	 */
	@Nonnull
	ExecutorService createDefaultExecutorService(final int threadPoolSize, final int keepAliveTime);

	/**
	 * @return A (new) {@link Transport} for the given session from the SMTP connection pool.
	 */
	@Nonnull
	LifecycleDelegatingTransport acquireTransport(@Nonnull Session session);

	/**
	 * Shuts down connection pool(s) and closes remaining open connections. Waits until all connections still in use become available again to deallocate them as well.
	 */
	@Nonnull
	Future<?> shutdownConnectionPools();
}