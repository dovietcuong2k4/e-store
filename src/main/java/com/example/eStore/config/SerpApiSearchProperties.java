package com.example.eStore.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "serpapi-search")
public class SerpApiSearchProperties {
    private String apiKey;
    private String apiUrl = "https://serpapi.com/search.json";
    private int timeoutSeconds = 10;
}

