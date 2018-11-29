package demo;

import org.simplejavamail.internal.clisupport.CliSupport;
import org.simplejavamail.mailer.ServerConfig;
import testutil.ConfigLoaderTestHelper;

import static java.util.Objects.requireNonNull;

public class CliDemoApp extends DemoAppBase {
	public static void main(String[] args) {
		ConfigLoaderTestHelper.clearConfigProperties();
		
		ServerConfig serverConfig = requireNonNull(mailerTLS.getServerConfig());
		
		demoTestConnection(serverConfig);
		demoSend(serverConfig);
	}
	
	private static void demoTestConnection(ServerConfig serverConfig) {
		CliSupport.runCLI(new String[]{
				"connect",
				"--mailer:withSMTPServer", "smtp.gmail.com", "587", serverConfig.getUsername(), serverConfig.getPassword(),
				"--mailer:withTransportStrategy", "SMTP_TLS"
		});
	}
	
	private static void demoSend(ServerConfig serverConfig) {
		CliSupport.runCLI(new String[]{
				"send",
				"--email:forwarding", "src/test/resources/test-messages/HTML mail with replyto and attachment and embedded image.msg",
				"--email:from", "Test sender", YOUR_GMAIL_ADDRESS,
				"--email:to", "Test Receiver", YOUR_GMAIL_ADDRESS,
				"--mailer:withSMTPServer", "smtp.gmail.com", "587", serverConfig.getUsername(), serverConfig.getPassword(),
				"--mailer:withTransportStrategy", "SMTP_TLS"
		});
	}
}