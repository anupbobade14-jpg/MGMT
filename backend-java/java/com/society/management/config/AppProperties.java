package com.society.management.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Getter @Setter
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private Upload upload = new Upload();
    private Mail mail = new Mail();
    private Maintenance maintenance = new Maintenance();
    private Frontend frontend = new Frontend();

    @Getter @Setter public static class Jwt {
        private String secret;
        private long expirationMs;
        private long refreshExpirationMs;
        private String issuer;
    }
    @Getter @Setter public static class Cors {
        private String allowedOrigins;
    }
    @Getter @Setter public static class Upload {
        private String baseDir;
        private String paymentProofs;
        private String documents;
        private String receipts;
        private String allowedImageTypes;
        private int maxFileSizeMb;
    }
    @Getter @Setter public static class Mail {
        private String from;
        private String fromName;
        private boolean enabled;
    }
    @Getter @Setter public static class Maintenance {
        private int defaultDueDay;
        private java.math.BigDecimal defaultLateFee;
        private String reminderCron;
        private String generationCron;
    }
    @Getter @Setter public static class Frontend {
        private String baseUrl;
    }
}
