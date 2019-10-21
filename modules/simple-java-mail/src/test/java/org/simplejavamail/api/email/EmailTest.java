package org.simplejavamail.api.email;

import org.junit.Test;
import org.simplejavamail.config.ConfigLoader;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.util.TestDataHelper;
import testutil.ConfigLoaderTestHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class EmailTest {

	@Test
	public void testSerialization() throws IOException {
		ConfigLoaderTestHelper.clearConfigProperties();

		Email e = EmailBuilder.startingBlank()
				.from("lollypop", "lol.pop@somemail.com")
				.withReplyTo("lollypop-reply", "lol.pop.reply@somemail.com")
				.withBounceTo("lollypop-bounce", "lol.pop.bounce@somemail.com")
				.to("C.Cane", "candycane@candyshop.org")
				.withPlainText("We should meet up!")
				.withHTMLText("<b>We should meet up!</b><img src='cid:thumbsup'>")
				.withSubject("hey")
				.withDispositionNotificationTo("simple@address.com")
				.withReturnReceiptTo("Complex Email", "simple@address.com")
				.withHeader("dummyHeader", "dummyHeaderValue")
				.buildEmail();

		OutputStream fileOut = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(e);
		out.close();
		fileOut.close();
	}
}