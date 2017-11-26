package testutil;

import org.simplejavamail.email.EmailBuilder;

import javax.annotation.Nullable;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static org.simplejavamail.converter.EmailConverter.outlookMsgToEmailBuilder;

public class EmailHelper {
	
	public static EmailBuilder createDummyEmailBuilder(boolean includeSubjectAndBody, boolean basicFields, boolean includeCustomHeaders) throws IOException {
		return createDummyEmailBuilder(null, includeSubjectAndBody, basicFields, includeCustomHeaders);
	}
	
	public static EmailBuilder createDummyEmailBuilder(@Nullable String id, boolean includeSubjectAndBody, boolean basicFields, boolean includeCustomHeaders)
			throws IOException {
		EmailBuilder builder = EmailBuilder.builder()
				.id(id)
				.from("lollypop", "lol.pop@somemail.com")
				// don't forget to add your own address here ->
				.to("C.Cane", "candycane@candyshop.org");
		
		if (!basicFields) {
			// normally not needed, but for the test it is because the MimeMessage will
			// have it added automatically as well, so the parsed Email will also have it then
			builder = builder
				.replyTo("lollypop-reply", "lol.pop.reply@somemail.com")
				.bounceTo("lollypop-bounce", "lol.pop.bounce@somemail.com");
		}
		if (includeSubjectAndBody) {
			builder = builder
				.subject("hey")
				.text("We should meet up!")
				.textHTML("<b>We should meet up!</b><img src='cid:thumbsup'>");
		}

		if (includeCustomHeaders) {
			builder = builder
				.addHeader("dummyHeader", "dummyHeaderValue")
				.withDispositionNotificationTo("simple@address.com")
				.withReturnReceiptTo("Complex Email", "simple@address.com");
		}

		// add two text files in different ways and a black thumbs up embedded image ->
		ByteArrayDataSource namedAttachment = new ByteArrayDataSource("Black Tie Optional", "text/plain");
		namedAttachment.setName("dresscode.txt"); // normally not needed, but otherwise the equals will fail
		String base64String = "iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAABeElEQVRYw2NgoAAYGxu3GxkZ7TY1NZVloDcAWq4MxH+B+D8Qv3FwcOCgtwM6oJaDMTAUXOhmuYqKCjvQ0pdoDrCnmwNMTEwakC0H4u8GBgYC9Ap6DSD+iewAoIPm0ctyLqBlp9F8/x+YE4zpYT8T0LL16JYD8U26+B7oyz4sloPwenpYno3DchCeROsUbwa05A8eB3wB4kqgIxOAuArIng7EW4H4EhC/B+JXQLwDaI4ryZaDSjeg5mt4LCcFXyIn1fdSyXJQVt1OtMWGhoai0OD8T0W8GohZifE1PxD/o7LlsPLiFNAKRrwOABWptLAcqc6QGDAHQEOAYaAc8BNotsJAOgAUAosG1AFA/AtUoY3YEFhKMAvS2AE7iC1+WaG1H6gY3gzE36hUFJ8mqzbU1dUVBBqQBzTgIDQRkWo5qCZdpaenJ0Zx1aytrc0DDB0foIG1oAYKqC0IZK8D4n1AfA6IzwPxXpCFoGoZVEUDaRGGUTAKRgEeAAA2eGJC+ETCiAAAAABJRU5ErkJggg==";
		
		return builder
				.addAttachment("dresscode.txt", namedAttachment)
				.addAttachment("location.txt", "On the moon!".getBytes(Charset.defaultCharset()), "text/plain")
				.embedImage("thumbsup", parseBase64Binary(base64String), "image/png");
	}
	
	public static EmailBuilder readOutlookMessage(final String filePath) {
		InputStream resourceAsStream = EmailHelper.class.getClassLoader().getResourceAsStream(filePath);
		return outlookMsgToEmailBuilder(resourceAsStream);
	}
	
	public static String normalizeText(String text) {
		return text.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
	}
}
