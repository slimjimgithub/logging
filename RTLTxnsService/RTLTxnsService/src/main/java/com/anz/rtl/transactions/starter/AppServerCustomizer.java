package com.anz.rtl.transactions.starter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.stereotype.Component;

@Component
public class AppServerCustomizer implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    @Value("${server.servlet.context-path:/banking}")
    private String serverContextPath;
    
    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        factory.setContextPath(serverContextPath);
        
    }

}
