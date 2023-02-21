package com.anz.rtl.transactions;

import java.util.Base64;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableFeignClients
@EnableSwagger2
@EnableAsync
public class RtlTxnsServiceApplication {

	private static final Logger LOG = LoggerFactory.getLogger(RtlTxnsServiceApplication.class);
	
	@Value("${spring.datasource.password.encrypt.required:true}")
	private boolean encryptPWDRequired;
	
	@Value("${spring.datasource.url}")
	private String dataSourceUrl;

	@Value("${spring.datasource.username}")
	private String userName;

	@Value("${spring.datasource.password}")
	private String password;

	@Value("${spring.datasource.driver-class-name}")
	private String driverClass;

	@Value("${spring.datasource.cachePrepStmts}")
	private boolean cachePreparedStatements;

	@Value("${spring.datasource.prepStmtCacheSize}")
	private int cachePreparedStatementsSize;

	@Value("${spring.datasource.hikari.maximum-pool-size}")
	private String maxPoolSize;

	@Value("${spring.datasource.hikari.connection-timeout}")
	private String timeOut;

	@Value("${spring.datasource.hikari.minimum-idle}")
	private String minIdle;

	@Value("${spring.datasource.hikari.idle-timeout}")
	private String idleTimeOut;

	@Value("${spring.datasource.hikari.max-lifetime}")
	private String maxLifeTime;
	
    public static void main(String... args) {
        SpringApplication.run(RtlTxnsServiceApplication.class, args);
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:application");
        messageSource.setDefaultEncoding("UTF-8");
        bean.setValidationMessageSource(messageSource);
        return bean;
    }
    
    @Bean(name = "dataSource", destroyMethod = "close")
	@Primary
	public DataSource dataSource() {
		LOG.info("Configuring Hikari DataSource");
		HikariConfig dataSourceConfig = new HikariConfig();
		dataSourceConfig.setDriverClassName(driverClass);
		dataSourceConfig.setJdbcUrl(dataSourceUrl);
		dataSourceConfig.setUsername(userName);
		dataSourceConfig.setPassword(decrypt(password));
		dataSourceConfig.setMaximumPoolSize(Integer.parseInt(maxPoolSize));
		dataSourceConfig.setMinimumIdle(Integer.parseInt(minIdle));
		dataSourceConfig.setMaxLifetime(Integer.parseInt(maxLifeTime));
		dataSourceConfig.setConnectionTimeout(Long.parseLong(timeOut));
		dataSourceConfig.setIdleTimeout(Long.parseLong(idleTimeOut));
		dataSourceConfig.addDataSourceProperty("cachePrepStmts", cachePreparedStatements);
		dataSourceConfig.addDataSourceProperty("prepStmtCacheSize", cachePreparedStatementsSize);
		dataSourceConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "100");
		return new HikariDataSource(dataSourceConfig);
	}
    
    public String decrypt(String decryptedData) {
		return encryptPWDRequired ? new String(Base64.getDecoder().decode(decryptedData)) : decryptedData;
	}

	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean
	public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
		return new NamedParameterJdbcTemplate(dataSource);
	}

	@Primary
	@Bean(name = "txnManager")
	public PlatformTransactionManager transactionManager(DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

	@Bean
	public TransactionTemplate transactionTemplate(PlatformTransactionManager txnManager) {
		return new TransactionTemplate(txnManager);
	}

}
