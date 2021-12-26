package org.simplejavamail.converter.internal.mimemessage;

import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import org.assertj.core.api.ThrowableAssert;
import org.jetbrains.annotations.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.simplejavamail.api.email.AttachmentResource;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.converter.EmailConverter;
import org.simplejavamail.internal.modules.ModuleLoader;
import org.simplejavamail.internal.util.MiscUtil;
import testutil.ConfigLoaderTestHelper;
import testutil.EmailHelper;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MiscUtil.class)
@PowerMockIgnore("javax.management.*")
public class MimeMessageHelperTest {
	
	@Before
	public void setup() {
		ModuleLoader.clearLoadedModules();
		ConfigLoaderTestHelper.clearConfigProperties();
		ModuleLoader._forceRecheckModule();
	}
	
	@Test
	public void determineResourceName1()
			throws IOException {
		AttachmentResource resource1 = new AttachmentResource(null, getDataSource("blahblah"));
		assertThat(MimeMessageHelper.determineResourceName(resource1, true)).isEqualTo("blahblah");
	}

	@Test
	public void determineResourceName2()
			throws IOException {
		AttachmentResource resource2 = new AttachmentResource(null, getDataSource("blahblah.txt"));
		assertThat(MimeMessageHelper.determineResourceName(resource2, true)).isEqualTo("blahblah.txt");
	}
	
	@Test
	public void determineResourceName3()
			throws IOException {
		AttachmentResource resource3 = new AttachmentResource("the resource", getDataSource(null));
		assertThat(MimeMessageHelper.determineResourceName(resource3, true)).isEqualTo("the resource");
	}
	
	@Test
	public void determineResourceName4()
			throws IOException {
		AttachmentResource resource4 = new AttachmentResource("the resource", getDataSource("blahblah.txt"));
		assertThat(MimeMessageHelper.determineResourceName(resource4, true)).isEqualTo("the resource.txt");
	}
	
	@Test
	public void determineResourceName5()
			throws IOException {
		AttachmentResource resource5 = new AttachmentResource("the resource", getDataSource("blahblah"));
		assertThat(MimeMessageHelper.determineResourceName(resource5, true)).isEqualTo("the resource");
	}
	
	@Test
	public void determineResourceName6()
			throws IOException {
		AttachmentResource resource6 = new AttachmentResource("the resource.txt", getDataSource("blahblah.txt"));
		assertThat(MimeMessageHelper.determineResourceName(resource6, true)).isEqualTo("the resource.txt");
	}
	
	@Test
	public void determineResourceName7()
			throws IOException {
		AttachmentResource resource7 = new AttachmentResource("the resource.txt", getDataSource("blahblah"));
		assertThat(MimeMessageHelper.determineResourceName(resource7, true)).isEqualTo("the resource.txt");
	}
	
	@Test
	public void determineResourceName_ignoreExtensionFromResource()
			throws IOException {
		AttachmentResource resource7 = new AttachmentResource("the resource.txt", getDataSource("blahblah.1/www/get?id=3"));
		assertThat(MimeMessageHelper.determineResourceName(resource7, true)).isEqualTo("the resource.txt");
	}
	
	private ByteArrayDataSource getDataSource(@Nullable String name)
			throws IOException {
		ByteArrayDataSource ds = new ByteArrayDataSource("", "text/text");
		ds.setName(name);
		return ds;
	}
	
	@Test
	public void testSignMessageWithDKIM_ShouldFailSpecificallyBecauseItWillTryToSign()
			throws IOException, ClassNotFoundException {
		final Email email = EmailHelper.createDummyEmailBuilder(true, false, false, true, false, false)
				.signWithDomainKey("dummykey", "moo.com", "selector")
				.buildEmail();
		
		assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
			@Override
			public void call() {
				EmailConverter.emailToMimeMessage(email);
			}
		})
				.isInstanceOf(Class.forName("org.simplejavamail.internal.dkimsupport.DKIMSigningException"))
				.hasMessage("Error signing MimeMessage with DKIM");
	}
	
	@Test
	public void testSignMessageWithDKIM_ShouldFailSpecificallyBecauseDKIMLibraryIsMissing()
			throws IOException, ClassNotFoundException {
		final Email email = EmailHelper.createDummyEmailBuilder(true, false, false, true, false, false)
				.signWithDomainKey("dummykey", "moo.com", "selector")
				.buildEmail();
		
		PowerMockito.mockStatic(MiscUtil.class);
		BDDMockito.given(MiscUtil.classAvailable("oorg.simplejavamail.internal.dkimsupport.DKIMSigner")).willReturn(false);
		BDDMockito.given(MiscUtil.encodeText(anyString())).willCallRealMethod();

		assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
			@Override
			public void call() {
				EmailConverter.emailToMimeMessage(email);
			}
		})
				.hasMessage("DKIM module not found, make sure it is on the classpath (https://github.com/bbottema/simple-java-mail/tree/develop/modules/dkim-module)");
		
		PowerMockito.mockStatic(MiscUtil.class);
		BDDMockito.given(MiscUtil.classAvailable("org.simplejavamail.internal.dkimsupport.DKIMSigner")).willCallRealMethod();
		BDDMockito.given(MiscUtil.encodeText(anyString())).willCallRealMethod();
		
		assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
			@Override
			public void call() {
				EmailConverter.emailToMimeMessage(email);
			}
		})
				.isInstanceOf(Class.forName("org.simplejavamail.internal.dkimsupport.DKIMSigningException"))
				.hasMessage("Error signing MimeMessage with DKIM");
	}

	@Test
	public void filenameWithSpaceEncoding() throws IOException, MessagingException {
		final String fileName = "file name.txt";
		final Email email = EmailHelper.createDummyEmailBuilder(true, true, false, false, false, false)
				.clearAttachments().withAttachment(fileName, "abc".getBytes(), "text/plain").buildEmail();
		final MimeMessage mimeMessage = EmailConverter.emailToMimeMessage(email);
		final BodyPart bodyPart = ((MimeMultipart) mimeMessage.getContent()).getBodyPart(1);
		ContentType ct = new ContentType(bodyPart.getHeader("Content-Type")[0]);
		assertThat(ct.getParameter("filename")).isEqualTo(fileName);
		assertThat(bodyPart.getFileName()).isEqualTo(fileName);
	}
}