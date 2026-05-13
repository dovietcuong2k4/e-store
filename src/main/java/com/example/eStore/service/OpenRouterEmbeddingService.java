package com.example.eStore.service;

import com.example.eStore.config.OpenRouterProperties;
import com.example.eStore.entity.Product;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenRouterEmbeddingService {
    private final OpenRouterProperties openRouterProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String buildEmbeddingJson(Product product) throws IOException, InterruptedException {
        return toJson(generateEmbedding(buildEmbeddingSource(product)));
    }

    public double[] generateEmbedding(String text) throws IOException, InterruptedException {
        if (!hasApiKey()) {
            throw new IllegalStateException("OpenRouter API key is missing");
        }

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", openRouterProperties.getEmbeddingModel());
        requestBody.put("input", List.of(text));

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(openRouterProperties.getTimeoutSeconds()))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(openRouterProperties.getEmbeddingsUrl()))
                .timeout(Duration.ofSeconds(openRouterProperties.getTimeoutSeconds()))
                .header("Authorization", "Bearer " + openRouterProperties.getApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("OpenRouter embeddings request failed with HTTP " + response.statusCode());
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode vectorNode = root.path("data").path(0).path("embedding");
        if (!vectorNode.isArray()) {
            throw new IllegalStateException("OpenRouter embeddings response did not contain a vector");
        }

        double[] vector = new double[vectorNode.size()];
        for (int i = 0; i < vectorNode.size(); i++) {
            vector[i] = vectorNode.get(i).asDouble();
        }
        return vector;
    }

    public double[] parseEmbeddingJson(String embeddingJson) throws IOException {
        if (embeddingJson == null || embeddingJson.isBlank()) {
            return new double[0];
        }

        JsonNode node = objectMapper.readTree(embeddingJson);
        if (!node.isArray()) {
            return new double[0];
        }

        double[] vector = new double[node.size()];
        for (int i = 0; i < node.size(); i++) {
            vector[i] = node.get(i).asDouble();
        }
        return vector;
    }

    public String toJson(double[] vector) throws IOException {
        return objectMapper.writeValueAsString(vector);
    }

    private String buildEmbeddingSource(Product product) {
        StringBuilder builder = new StringBuilder();
        append(builder, "name", product.getName());
        append(builder, "description", product.getDescription());
        append(builder, "category", product.getCategory() != null ? product.getCategory().getName() : null);
        append(builder, "brand", product.getBrand() != null ? product.getBrand().getName() : null);
        append(builder, "cpu", product.getCpu());
        append(builder, "ram", product.getRam());
        append(builder, "screen", product.getScreen());
        append(builder, "operatingSystem", product.getOperatingSystem());
        append(builder, "batteryCapacity", product.getBatteryCapacity());
        append(builder, "design", product.getDesign());
        append(builder, "warrantyInfo", product.getWarrantyInfo());

        if (product.getPrice() != null) {
            append(builder, "price", product.getPrice() + " VND");
            append(builder, "priceRange", priceRange(product.getPrice()));
        }

        return builder.toString().trim();
    }

    private void append(StringBuilder builder, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append('\n');
        }
        builder.append(label).append(": ").append(value.trim());
    }

    private String priceRange(Long price) {
        if (price == null) {
            return null;
        }

        long million = 1_000_000L;
        if (price < 5 * million) return "budget";
        if (price < 12 * million) return "midrange";
        if (price < 25 * million) return "upper-midrange";
        return "premium";
    }

    private boolean hasApiKey() {
        return openRouterProperties.getApiKey() != null && !openRouterProperties.getApiKey().isBlank();
    }
}