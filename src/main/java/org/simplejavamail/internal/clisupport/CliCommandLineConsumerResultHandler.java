package org.simplejavamail.internal.clisupport;

import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.email.EmailPopulatingBuilder;
import org.simplejavamail.internal.clisupport.model.CliBuilderApiType;
import org.simplejavamail.internal.clisupport.model.CliReceivedOptionData;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.MailerGenericBuilder;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

class CliCommandLineConsumerResultHandler {
	
	private static final Logger LOGGER = getLogger(CliCommandLineConsumerResultHandler.class);
	
	@SuppressWarnings("deprecation")
	static void processCliResult(List<CliReceivedOptionData> cliReceivedOptionData) {
		LOGGER.debug("invoking Builder API in order of provided options...");
		
		EmailPopulatingBuilder emailBuilder = invokeBuilderApi(cliReceivedOptionData, CliBuilderApiType.EMAIL, EmailBuilder._createForCli());
		MailerGenericBuilder<?> mailerBuilder = invokeBuilderApi(cliReceivedOptionData, CliBuilderApiType.MAILER, MailerBuilder._createForCli());
		
		mailerBuilder.buildMailer()
			.sendMail(emailBuilder.buildEmail());
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T invokeBuilderApi(List<CliReceivedOptionData> cliReceivedOptionData, CliBuilderApiType builderApiType, Object initialBuilderInstance) {
		LOGGER.debug("\t{}", initialBuilderInstance.getClass().getSimpleName());
		Object currentBuilder = initialBuilderInstance;
		for (CliReceivedOptionData option : cliReceivedOptionData) {
			if (option.determineTargetBuilderApi() == builderApiType) {
				try {
					LOGGER.debug("\t\t.{}({})", option.getDeclaredOptionSpec().getSourceMethod().getName(), option.getProvidedOptionValues());
					currentBuilder = option.getDeclaredOptionSpec().getSourceMethod().invoke(currentBuilder, option.getProvidedOptionValues().toArray());
				} catch (IllegalAccessException | InvocationTargetException e) {
					throw new CliExecutionException(CliExecutionException.ERROR_INVOKING_BUILDER_API, e);
				}
			}
		}
		return (T) currentBuilder;
	}
}
