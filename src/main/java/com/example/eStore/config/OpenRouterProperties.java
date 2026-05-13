package com.example.eStore.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "openrouter")
public class OpenRouterProperties {
    private String apiKey;
    private String model = "openai/gpt-4o-mini";
    private String responsesUrl = "https://openrouter.ai/api/v1/responses";
    private String embeddingsUrl = "https://openrouter.ai/api/v1/embeddings";
    private String embeddingModel = "openai/text-embedding-3-small";
    private int timeoutSeconds = 30;
    private int maxOutputTokens = 700;
    private int maxCatalogProducts = 40;
}
