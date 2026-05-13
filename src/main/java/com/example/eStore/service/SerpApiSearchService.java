package com.example.eStore.service;

import com.example.eStore.config.SerpApiSearchProperties;
import com.example.eStore.dto.ExternalSource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SerpApiSearchService {
    private final SerpApiSearchProperties serpApiSearchProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean hasApiKey() {
        return serpApiSearchProperties.getApiKey() != null
                && !serpApiSearchProperties.getApiKey().isEmpty()
                && !serpApiSearchProperties.getApiKey().contains("null");
    }

    public List<ExternalSource> searchProductSources(String productName, int count) throws IOException, InterruptedException {
        if (!hasApiKey()) {
            return List.of();
        }

        String query = productName + " review";
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String encodedApiKey = URLEncoder.encode(serpApiSearchProperties.getApiKey(), StandardCharsets.UTF_8);
        String url = serpApiSearchProperties.getApiUrl()
                + "?q=" + encodedQuery
                + "&num=" + count
                + "&api_key=" + encodedApiKey;

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(serpApiSearchProperties.getTimeoutSeconds()))
                .build();

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(serpApiSearchProperties.getTimeoutSeconds()))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.warn("SerpAPI returned status code: {} - {}", response.statusCode(), response.body());
            throw new RuntimeException("SerpAPI call failed");
        }

        return parseSourcesFromResponse(response.body(), count);
    }

    private List<ExternalSource> parseSourcesFromResponse(String responseBody, int count) throws IOException {
        JsonNode responseJson = objectMapper.readTree(responseBody);
        JsonNode resultsArray = responseJson.path("organic_results");

        List<ExternalSource> sources = new ArrayList<>();

        if (resultsArray.isArray()) {
            for (JsonNode resultNode : resultsArray) {
                if (sources.size() >= count) {
                    break;
                }

                String title = resultNode.path("title").asText(null);
                String url = resultNode.path("link").asText(null);
                String snippet = resultNode.path("snippet").asText("");
                String website = resultNode.path("source").asText(null);

                if (website == null && url != null) {
                    try {
                        String host = URI.create(url).getHost();
                        if (host != null) {
                            website = host.replaceFirst("^www\\.", "");
                        }
                    } catch (Exception e) {
                        log.debug("Failed to parse website from url {}: {}", url, e.getMessage());
                    }
                }

                if (title != null && url != null) {
                    sources.add(ExternalSource.builder()
                            .title(title)
                            .url(url)
                            .website(website != null ? website : "")
                            .snippet(snippet)
                            .build());
                }
            }
        }

        return sources;
    }
}

