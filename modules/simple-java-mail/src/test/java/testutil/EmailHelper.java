package testutil;

import org.assertj.core.util.Lists;
import org.simplejavamail.api.email.EmailPopulatingBuilder;
import org.simplejavamail.api.mailer.CustomMailer;
import org.simplejavamail.api.mailer.config.LoadBalancingStrategy;
import org.simplejavamail.api.mailer.config.OperationalConfig;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.email.internal.InternalEmailPopulatingBuilder;
import org.simplejavamail.internal.smimesupport.model.OriginalSmimeDetailsImpl;
import org.simplejavamail.internal.util.SimpleOptional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static java.util.UUID.randomUUID;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static org.simplejavamail.api.mailer.config.LoadBalancingStrategy.ROUND_ROBIN;
import static org.simplejavamail.converter.EmailConverter.outlookMsgToEmailBuilder;
import static org.simplejavamail.internal.util.Preconditions.checkNonEmptyArgument;

public class EmailHelper {

	public static EmailPopulatingBuilder createDummyEmailBuilder(boolean includeSubjectAndBody, boolean basicFields, boolean includeCustomHeaders, boolean useSmimeDetailsImplFromSmimeModule)
			throws IOException {
		return createDummyEmailBuilder(null, includeSubjectAndBody, basicFields, includeCustomHeaders, useSmimeDetailsImplFromSmimeModule);
	}

	public static EmailPopulatingBuilder createDummyEmailBuilder(@Nullable String id, boolean includeSubjectAndBody, boolean basicFields, boolean includeCustomHeaders,
			boolean useSmimeDetailsImplFromSmimeModule)
			throws IOException {
		EmailPopulatingBuilder builder = EmailBuilder.startingBlank()
				.fixingMessageId(id)
				.from("lollypop", "lol.pop@somemail.com")
				// don't forget to add your own address here ->
				.to("C.Cane", "candycane@candyshop.org");

		if (!basicFields) {
			// normally not needed, but for the test it is because the MimeMessage will
			// have it added automatically as well, so the parsed Email will also have it then
			builder = builder
					.withReplyTo("lollypop-reply", "lol.pop.reply@somemail.com")
					.withBounceTo("lollypop-bounce", "lol.pop.bounce@somemail.com");
		}
		if (includeSubjectAndBody) {
			builder = builder
					.withSubject("hey")
					.withPlainText("We should meet up!")
					.withHTMLText("<b>We should meet up!</b><img src='cid:thumbsup'>");
		}

		if (includeCustomHeaders) {
			builder = builder
					.withHeader("dummyHeader", "dummyHeaderValue")
					.withHeader("anotherDummyHeader", "anotherDummyHeaderValue")
					.withDispositionNotificationTo("simple@address.com")
					.withReturnReceiptTo("Complex Email", "simple@address.com");
		}

		// add two text files in different ways and a black thumbs up embedded image ->
		ByteArrayDataSource namedAttachment = new ByteArrayDataSource("Black Tie Optional", "text/plain");
		namedAttachment.setName("dresscode-ignored-because-of-override.txt");
		String base64String = "iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAABeElEQVRYw2NgoAAYGxu3GxkZ7TY1NZVloDcAWq4MxH+B+D8Qv3FwcOCgtwM6oJaDMTAUXOhmuYqKCjvQ0pdoDrCnmwNMTEwakC0H4u8GBgYC9Ap6DSD+iewAoIPm0ctyLqBlp9F8/x+YE4zpYT8T0LL16JYD8U26+B7oyz4sloPwenpYno3DchCeROsUbwa05A8eB3wB4kqgIxOAuArIng7EW4H4EhC/B+JXQLwDaI4ryZaDSjeg5mt4LCcFXyIn1fdSyXJQVt1OtMWGhoai0OD8T0W8GohZifE1PxD/o7LlsPLiFNAKRrwOABWptLAcqc6QGDAHQEOAYaAc8BNotsJAOgAUAosG1AFA/AtUoY3YEFhKMAvS2AE7iC1+WaG1H6gY3gzE36hUFJ8mqzbU1dUVBBqQBzTgIDQRkWo5qCZdpaenJ0Zx1aytrc0DDB0foIG1oAYKqC0IZK8D4n1AfA6IzwPxXpCFoGoZVEUDaRGGUTAKRgEeAAA2eGJC+ETCiAAAAABJRU5ErkJggg==";

		InternalEmailPopulatingBuilder internalBuilder = ((InternalEmailPopulatingBuilder) builder
				.withAttachment("dresscode.txt", namedAttachment)
				.withAttachment("location.txt", "On the moon!".getBytes(Charset.defaultCharset()), "text/plain")
				.withEmbeddedImage("thumbsup", parseBase64Binary(base64String), "image/png"))
				.withDecryptedAttachments(builder.getAttachments());

		if (useSmimeDetailsImplFromSmimeModule) {
			internalBuilder.withOriginalSmimeDetails(OriginalSmimeDetailsImpl.builder().build());
		}

		return internalBuilder;
	}

	public static EmailPopulatingBuilder readOutlookMessage(final String filePath) {
		InputStream resourceAsStream = EmailHelper.class.getClassLoader().getResourceAsStream(filePath);
		return outlookMsgToEmailBuilder(checkNonEmptyArgument(resourceAsStream, "resourceAsStream")).getEmailBuilder();
	}

	@NotNull
	@SuppressWarnings("SameParameterValue")
	public static OperationalConfig createDummyOperationalConfig(@Nullable List<String> hostsToTrust, boolean trustAllSSLHost, boolean verifyServerIdentity) {
		return createDummyOperationalConfig(
				/*0*/false,
				/*1*/new Properties(),
				/*2*/0,
				/*3*/10,
				/*4*/1000,
				/*5*/randomUUID(),
				/*6*/0,
				/*7*/1,
				/*8*/5000,
				/*9*/10000,
				/*10*/ROUND_ROBIN,
				/*11*/false,
				/*12*/false,
				/*13*/SimpleOptional.ofNullable(hostsToTrust).orElse(Lists.<String>emptyList()),
				/*14*/trustAllSSLHost,
				/*15*/verifyServerIdentity,
				/*16*/newSingleThreadExecutor(),
				/*17*/null);
	}

	@NotNull
	@SuppressWarnings("SameParameterValue")
	public static OperationalConfig createDummyOperationalConfig(
			/*0*/final boolean async,
			/*1*/@Nullable final Properties properties,
			/*2*/final int sessionTimeout,
			/*3*/final int threadPoolSize,
			/*4*/final int threadPoolKeepAliveTime,
			/*5*/@NotNull final UUID clusterKey,
			/*6*/final int connectionPoolCoreSize,
			/*7*/final int connectionPoolMaxSize,
			/*8*/final int connectionPoolClaimTimeoutMillis,
			/*9*/final int connectionPoolExpireAfterMillis,
			/*10*/@NotNull final LoadBalancingStrategy connectionPoolLoadBalancingStrategy,
			/*11*/final boolean transportModeLoggingOnly,
			/*12*/final boolean debugLogging,
			/*13*/@NotNull final List<String> sslHostsToTrust,
			/*14*/final boolean trustAllSSLHost,
			/*15*/final boolean verifyingServerIdentity,
			/*16*/@NotNull final ExecutorService executorService,
			/*17*/@Nullable final CustomMailer customMailer) {
		try {
			Constructor<?> constructor = Class.forName("org.simplejavamail.mailer.internal.OperationalConfigImpl").getDeclaredConstructors()[0];
			constructor.setAccessible(true);
			return (OperationalConfig) constructor.newInstance(
					/*0*/async,
					/*1*/properties,
					/*2*/sessionTimeout,
					/*3*/threadPoolSize,
					/*4*/threadPoolKeepAliveTime,
					/*5*/clusterKey,
					/*6*/connectionPoolCoreSize,
					/*7*/connectionPoolMaxSize,
					/*8*/connectionPoolClaimTimeoutMillis,
					/*9*/connectionPoolExpireAfterMillis,
					/*10*/connectionPoolLoadBalancingStrategy,
					/*11*/transportModeLoggingOnly,
					/*12*/debugLogging,
					/*13*/sslHostsToTrust,
					/*14*/trustAllSSLHost,
					/*15*/verifyingServerIdentity,
					/*16*/executorService,
					/*17*/customMailer);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new AssertionError(e.getMessage(), e);
		}
	}
}
