package com.example.eStore.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "openai")
public class OpenAiProperties {
    private String apiKey;
    private String model = "gpt-5.4-mini";
    private String responsesUrl = "https://api.openai.com/v1/responses";
    private int timeoutSeconds = 30;
    private int maxOutputTokens = 700;
    private int maxCatalogProducts = 40;
}
