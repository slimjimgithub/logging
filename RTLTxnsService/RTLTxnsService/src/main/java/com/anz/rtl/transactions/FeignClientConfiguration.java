package com.anz.rtl.transactions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedActionException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

import feign.Client;
import feign.Request;
import feign.httpclient.ApacheHttpClient;

public class FeignClientConfiguration {

	private Logger logger = LoggerFactory.getLogger(FeignClientConfiguration.class);

	@Value("${rest.ssl.provider:com.sun.net.ssl.internal.ssl.Provider}")
	private String sunSSLProvider;

	@Value("${rest.ssl.password.encrypt.required:true}")
	private boolean encryptPWDRequired;

	@Value(value = "${rest.truststore.path:keystore/cisclient.jks}")
	private String restSslTSPATH;

	@Value(value = "${rest.keystore.path:keystore/cisclient.jks}")
	private String restSslKSPATH;

	@Value(value = "${rest.ssl.keystore.password:Y2hhbmdlaXQ=}")
	private String restSslKSPWD;

	@Value(value = "${rest.ssl.truststore.password:Y2hhbmdlaXQ=}")
	private String restSslTSPWD;

	@Value("${rest.ssl.protocol:TLS}")
	private String sslProtocol;

	@Value("${rest.ssl.verifyHostName:false}")
	private boolean verifyHostName;

	@Value("${rest.ssl.keystore.required:true}")
	private boolean isRestKSRequired;

	@Value("${rest.ssl.truststore.required:true}")
	private boolean isRestTSRequired;

	@Value("${rest.feign.connection.timeout:600000}")
	private int connectionTimeout;

	@Value("${rest.feign.read.timeout:600000}")
	private int readTimeout;

	@Value("${rest.ssl.max.connection:300}")
	private int maxConnection;

	@Value("${rest.ssl.max.con.per.route:200}")
	private int maxConnectionPerRoute;

	@Value("${rest.feign.time.to.live:14000}")
	private int timeToLive;

	@Value("${rest.feign.time.close.con:60000}")
	private int closeIdleConnections;

	@Value("${rest.ssl.enable}")
	private boolean sslEnable;

	@Value("${rest.ssl.retry.required:false}")
	private boolean autoRetryRequired;

	@Autowired
	private Environment env;

	@Autowired
	protected ResourceLoader resourceLoader;

	private static final String SPRING_CONFIG_LOCATION = "spring.config.location";

	public HttpClient getHttpClient() throws Exception {
		logger.info("HTTP client creatation started");
		RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", new PlainConnectionSocketFactory());

		if (sslEnable) {
			registryBuilder.register("https", new SSLConnectionSocketFactory(getSSLcontext(), getHostnameVerifier()));
			logger.info("SSL HTTP client created");
		} else {
			logger.info("Non-SSL HTTP client created");
		}

		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(
				registryBuilder.build());
		connManager.setMaxTotal(maxConnection);
		connManager.setDefaultMaxPerRoute(maxConnectionPerRoute);
		connManager.closeIdleConnections(closeIdleConnections, TimeUnit.MILLISECONDS);

		ConnectionKeepAliveStrategy keepAliveStrategy = (HttpResponse response, HttpContext context) -> {
			if (response != null) {
				HeaderElementIterator it = new BasicHeaderElementIterator(
						response.headerIterator(HTTP.CONN_KEEP_ALIVE));
				while (it.hasNext()) {
					HeaderElement he = it.nextElement();
					String param = he.getName();
					String value = he.getValue();
					if (value != null && "timeout".equalsIgnoreCase(param)) {
						return Long.parseLong(value) * 1000;
					}
				}
			}
			return timeToLive;
		};

		HttpClientBuilder httpclient = HttpClients.custom().setConnectionManager(connManager)
				.setKeepAliveStrategy(keepAliveStrategy);

		if (!autoRetryRequired) {
			httpclient.disableAutomaticRetries();
		}
		logger.info("HTTP client creatation end");
		return httpclient.build();
	}

	@Bean
	public Client getFeignClient() {
		try {
			return new ApacheHttpClient(getHttpClient());
		} catch (Exception e) {
			logger.error("ERROR: while creating feign client {}", e);
		}
		return new Client.Default(null, null);
	}

	public SSLContext getSSLcontext() throws NoSuchAlgorithmException, KeyStoreException, PrivilegedActionException,
			CertificateException, IOException, KeyManagementException, UnrecoverableKeyException {

		// This supports TLSv1.2
		SSLContext sslContext = SSLContext.getInstance(sslProtocol);
		TrustManagerFactory tmf = null;
		KeyManager[] keyManagers = null;
		try {
			Class.forName(sunSSLProvider);
		} catch (ClassNotFoundException e) {
		}
		if (isRestTSRequired) {
			KeyStore tStore = KeyStore.getInstance("JKS");
			tStore.load(getResource(env.getProperty(SPRING_CONFIG_LOCATION), restSslKSPATH),
					decrypt(restSslTSPWD).toCharArray());
			tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(tStore);
		}

		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		if (isRestKSRequired) {
			KeyStore kStore = KeyStore.getInstance(KeyStore.getDefaultType());
			kStore.load(getResource(env.getProperty(SPRING_CONFIG_LOCATION), restSslTSPATH),
					decrypt(restSslKSPWD).toCharArray());
			kmf.init(kStore, decrypt(restSslKSPWD).toCharArray());
			keyManagers = kmf.getKeyManagers();
		}

		if (keyManagers == null) {
			// 2-way TLS not required. Let JVM uses its default
			keyManagers = new KeyManager[] {};
		}

		sslContext.init(keyManagers, isRestTSRequired ? tmf.getTrustManagers() : trustAllCertificates(),
				new SecureRandom());

		return sslContext;
	}

	public static TrustManager[] trustAllCertificates() {

		return new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} };

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

	public HostnameVerifier getHostnameVerifier() {
		return (hostname, session) -> {
			if (verifyHostName) {
				HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
				return hv.verify(hostname, session);
			}
			return true;
		};
	}

	@Bean
	public Request.Options requestOptions(ConfigurableEnvironment env) {
		return new Request.Options(connectionTimeout, readTimeout);
	}

}