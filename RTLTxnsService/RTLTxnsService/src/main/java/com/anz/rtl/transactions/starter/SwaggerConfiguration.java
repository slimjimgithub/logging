package com.anz.rtl.transactions.starter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.swagger.annotations.SwaggerDefinition;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger Configurations for Balances Service
 * @author rajolic1
 *
 */
@Profile(value = {"swagger"})
@Configuration
@EnableSwagger2
@SwaggerDefinition (schemes = SwaggerDefinition.Scheme.HTTPS)
public class SwaggerConfiguration {

    /**
     * Docket bean for Swagger API
     * @return
     */
	
	private static final Set<String> DEFAULT_PRODUCES_AND_CONSUMES = new HashSet<String>(
			Arrays.asList("application/json"));
	
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2).useDefaultResponseMessages(false).groupName("rtltransactions")
                .select().apis(RequestHandlerSelectors.basePackage("com.anz.rtl.transactions.controller"))
                .paths(PathSelectors.any()).build().apiInfo(apiInfo()).produces(DEFAULT_PRODUCES_AND_CONSUMES)
				.consumes(DEFAULT_PRODUCES_AND_CONSUMES);
    }

    /**
     * ApiInfo - set the Balances API Details for Swagger API
     * @return
     */
    ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("Retail Banking Transactions").description(
                "Retail banking is a financial services term as part of financial technology that refers to: The use of open APIs that enable third party developers to build applications and services around the financial institution. The scope of API is to provide transaction history for customer accounts. Provides a list of transactions for the specified account includes support for filtering and pagination of the transaction data. Returns detailed information for a specific transaction and will include additional fields if the transaction was created via a NPP payment.")
                .license("ANZ Licence 1.0").licenseUrl("http://www.anz.com").version("1.0")
                .contact(new Contact("CIS Team", null, "cisdevelopment@anz.com")).build();
    }
}