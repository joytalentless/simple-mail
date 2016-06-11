

package org.codemonkey.simplejavamail.internal.socks.socksclient;

import org.codemonkey.simplejavamail.internal.socks.common.SocksException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;

public class SSLConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(SSLConfiguration.class);

	private final KeyStoreInfo keyStoreInfo;
	private final KeyStoreInfo trustKeyStoreInfo;

	@SuppressWarnings("SameParameterValue")
	public SSLConfiguration(KeyStoreInfo keyStoreInfo, KeyStoreInfo trustKeyStoreInfo) {
		this.keyStoreInfo = keyStoreInfo;
		this.trustKeyStoreInfo = trustKeyStoreInfo;
	}

	public SSLSocketFactory getSSLSocketFactory()
			throws SocksException {
		Util.checkNotNull(trustKeyStoreInfo, "trustKeyStoreInfo may not be null");
		KeyStore keyStore = null;
		try {
			SSLContext context = SSLContext.getInstance("SSL");
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
			KeyStore trustKeyStore = KeyStore.getInstance(trustKeyStoreInfo.getType());
			trustKeyStore.load(new FileInputStream(trustKeyStoreInfo.getKeyStorePath()), trustKeyStoreInfo.getPassword().toCharArray());
			trustManagerFactory.init(trustKeyStore);

			if (keyStoreInfo != null && keyStoreInfo.getKeyStorePath() != null) {
				KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
				keyStore = KeyStore.getInstance(keyStoreInfo.getType());
				keyStore.load(new FileInputStream(keyStoreInfo.getKeyStorePath()), keyStoreInfo.getPassword().toCharArray());
				keyManagerFactory.init(keyStore, keyStoreInfo.getPassword().toCharArray());

				context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
			} else {
				context.init(null, trustManagerFactory.getTrustManagers(), null);
			}

			if (keyStore != null) {
				LOGGER.info("SSL: Key store:{}", keyStoreInfo.getKeyStorePath());
			}
			LOGGER.info("SSL: Trust key store:{}", trustKeyStoreInfo.getKeyStorePath());
			return context.getSocketFactory();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new SocksException(e.getMessage());
		}
	}

}
