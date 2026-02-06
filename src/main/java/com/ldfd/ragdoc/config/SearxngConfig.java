package com.ldfd.ragdoc.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "searxng")
public class SearxngConfig {

    private String baseUrl;
    private Integer timeout;
    private Integer maxResults;
}
