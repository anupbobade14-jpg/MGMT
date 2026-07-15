package com.society.management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class LoggingConfig {

    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter f = new CommonsRequestLoggingFilter();
        f.setIncludeQueryString(true);
        f.setIncludeClientInfo(true);
        f.setMaxPayloadLength(2000);
        f.setIncludeHeaders(false);
        return f;
    }
}
