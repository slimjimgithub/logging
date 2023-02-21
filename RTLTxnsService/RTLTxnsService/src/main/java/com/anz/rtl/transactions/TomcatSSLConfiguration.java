package com.anz.rtl.transactions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import javax.net.ssl.TrustManagerFactory;

import org.apache.catalina.connector.Connector;
import org.apache.commons.lang3.StringUtils;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

@Configuration
public class TomcatSSLConfiguration implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

	private Logger logger = LoggerFactory.getLogger(TomcatSSLConfiguration.class);

	private static final String HTTPS = "https";
	private static final String JKS = "JKS";

	@Value("${rtl.tomcat.ssl.password.encrypt.required:true}")
	private boolean encryptPWDRequired;
	
	@Value(value = "${rtl.tomcat.ssl.port:28099}")
	private Integer sslPort;

	@Value(value = "${rtl.tomcat.ssl.enabled:true}")
	private boolean restSslEnabled;

	@Value(value = "${rtl.tomcat.ssl.trustClient:true}")
	private boolean trustClient;

	@Value("${rtl.tomcat.ssl.keystore.required:true}")
	private boolean isRestKSRequired;

	@Value("${rtl.tomcat.ssl.truststore.required:false}")
	private boolean isRestTSRequired;

	@Value(value = "${rtl.tomcat.truststore.path:keystore/key.jks}")
	private String restSslTSPATH;

	@Value(value = "${rtl.tomcat.keystore.path:keystore/cisserverdev.jks}")
	private String restSslKSPATH;

	@Value(value = "${rtl.tomcat.ssl.keystore.password:Y2hhbmdlaXQ=}")
	private String restSslKSPWD;

	@Value(value = "${rtl.tomcat.ssl.truststore.password:Y2hhbmdlaXQ=}")
	private String restSslTSPWD;

	private String SPRING_CONFIG_LOCATION = "spring.config.location";

	@Autowired
	private Environment env;

	@Autowired
	protected ResourceLoader resourceLoader;


	@Override
	public void customize(TomcatServletWebServerFactory factory) {
		factory.addConnectorCustomizers(new TomcatConnectorCustomizer() {

			@Override
			public void customize(Connector connector) {

				logger.info("Tomcat params :: {}", initParams());
				if (restSslEnabled) {
					connector.setPort(sslPort);

					// Enable SSL
					connector.setScheme(HTTPS);
					connector.setSecure(true);
					Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
					protocol.setSSLEnabled(true);

					// Keystore config
					if (isRestKSRequired) {
						String keystore = null;
						try {
							keystore = getResourceAbsolutePath(env.getProperty(SPRING_CONFIG_LOCATION), restSslKSPATH);
						} catch (IOException e) {
							logger.error("Error while getting path for keystore store file.");
						}
						protocol.setKeystoreFile(keystore);
						protocol.setKeystorePass(decrypt(restSslKSPWD));
						protocol.setKeystoreType(JKS);
					}

					// Truststore config
					if (isRestTSRequired) {
						String truststore = null;
						try {
							truststore = getResourceAbsolutePath(env.getProperty(SPRING_CONFIG_LOCATION),
									restSslTSPATH);
						} catch (IOException e) {
							logger.error("Error while getting path for trust store file.");
						}
						protocol.setTruststoreFile(truststore);
						protocol.setTruststorePass(decrypt(restSslTSPWD));
						protocol.setTruststoreAlgorithm(TrustManagerFactory.getDefaultAlgorithm());
						protocol.setTruststoreType(JKS);
						if (trustClient) {
							protocol.setClientAuth(SSLHostConfig.CertificateVerification.REQUIRED.name());
							protocol.setSSLVerifyClient(SSLHostConfig.CertificateVerification.REQUIRED.name());
						} else {
							protocol.setClientAuth(SSLHostConfig.CertificateVerification.OPTIONAL.name());
							protocol.setSSLVerifyClient(SSLHostConfig.CertificateVerification.OPTIONAL.name());
						}
					}
				}
			}
		});
	}

	public InputStream getResource(String dirLoc, String fileName) throws IOException {
		logger.info("getResource File Directory : {} - File Name : {} ", dirLoc, fileName);
		try {
			if (StringUtils.isNotBlank(dirLoc)) {
				return resourceLoader.getResource(dirLoc + File.separator + fileName).getInputStream();
			}
		} catch (IOException e) {
			logger.error("getResource Not able to find the key store file from  : {} ",
					dirLoc + File.separator + fileName, e);
			logger.info("getResource trying to load from class path");
			return resourceLoader.getResource("classpath:" + File.separator + fileName).getInputStream();
		}
		logger.debug("getResource Loading from class path...");
		return resourceLoader.getResource("classpath:" + File.separator + fileName).getInputStream();
	}

	public String getResourceAbsolutePath(String dirLoc, String fileName) throws IOException {
		logger.info("getResourceAbsolutePath File Directory : {} - File Name : {} ", dirLoc, fileName);
		try {
			if (StringUtils.isNotBlank(dirLoc)) {
				return dirLoc + File.separator + fileName;
			} else {
				logger.info(" Loading from class path to get getResourceAbsolutePath ...");
				final String fullPath = resourceLoader.getResource("classpath:" + File.separator + fileName).getURL()
						.toExternalForm();

				logger.info(" File path : {} ", fullPath);
				return fullPath;
			}
		} catch (IOException e) {

			logger.info(" Loading from class path to get getResourceAbsolutePath ...");
			final String fullPath = resourceLoader.getResource("classpath:" + File.separator + fileName).getURL()
					.toExternalForm();
			logger.info(" File path : {} ", fullPath);
			return fullPath;
		}
	}

	public String decrypt(String decryptedData) {
		return encryptPWDRequired ? new String(Base64.getDecoder().decode(decryptedData)) : decryptedData;
	}

	public String initParams() {
		return "TomcatSSLConfiguration [sslPort=" + sslPort + ", restSslEnabled="
				+ restSslEnabled + ", trustClient=" + trustClient + ", isRestKSRequired=" + isRestKSRequired
				+ ", isRestTSRequired=" + isRestTSRequired + ", restSslTSPATH=" + restSslTSPATH + ", restSslKSPATH="
				+ restSslKSPATH + ", restSslKSPWD=" + "XXXXX" + ", restSslTSPWD=" + "XXXXX" + "]";
	}
	
	
}
