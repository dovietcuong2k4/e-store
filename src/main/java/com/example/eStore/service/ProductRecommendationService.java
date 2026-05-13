package com.example.eStore.service;

import com.example.eStore.config.OpenRouterProperties;
import com.example.eStore.dto.response.ProductRecommendationResponse;
import com.example.eStore.dto.response.RecommendedProductDTO;
import com.example.eStore.entity.Product;
import com.example.eStore.repository.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductRecommendationService {
    private final ProductRepository productRepository;
    private final OpenRouterProperties openRouterProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public ProductRecommendationResponse getRecommendations(Long productId) {
        // Lấy sản phẩm hiện tại
        Product currentProduct = productRepository.findById(productId)
                .orElse(null);

        if (currentProduct == null) {
            return ProductRecommendationResponse.builder()
                    .recommendations(new ArrayList<>())
                    .aiEnabled(false)
                    .message("Sản phẩm không tồn tại")
                    .build();
        }

        // Lấy danh sách sản phẩm liên quan (cùng category, cùng brand)
        List<Product> relatedProducts = productRepository.findRelatedProducts(
                currentProduct.getCategory().getId(),
                currentProduct.getBrand().getId(),
                productId,
                PageRequest.of(0, 10)
        );

        if (relatedProducts.isEmpty()) {
            return ProductRecommendationResponse.builder()
                    .recommendations(new ArrayList<>())
                    .aiEnabled(false)
                    .message("Không có sản phẩm liên quan")
                    .build();
        }

        // Nếu không có API key, trả về fallback
        if (!hasApiKey()) {
            return fallbackRecommendation(relatedProducts);
        }

        // Gọi OpenRouter để xử lý
        try {
            List<RecommendedProductDTO> recommendations = askOpenRouterForRecommendations(
                    currentProduct,
                    relatedProducts
            );

            return ProductRecommendationResponse.builder()
                    .recommendations(recommendations)
                    .aiEnabled(true)
                    .message("AI đã chọn những sản phẩm tốt nhất cho bạn")
                    .build();
        } catch (Exception e) {
            log.warn("OpenRouter recommendation request failed, using fallback: {}", e.getMessage());
            return fallbackRecommendation(relatedProducts);
        }
    }

    private List<RecommendedProductDTO> askOpenRouterForRecommendations(
            Product currentProduct,
            List<Product> relatedProducts) throws IOException, InterruptedException {

        String prompt = buildRecommendationPrompt(currentProduct, relatedProducts);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", openRouterProperties.getModel());
        requestBody.put("max_tokens", openRouterProperties.getMaxOutputTokens());
        requestBody.put("temperature", 0.7);
        requestBody.put("messages", List.of(
                Map.of(
                        "role", "user",
                        "content", prompt
                )
        ));

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(openRouterProperties.getTimeoutSeconds()))
                .build();

        String requestBodyJson = objectMapper.writeValueAsString(requestBody);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))
                .timeout(Duration.ofSeconds(openRouterProperties.getTimeoutSeconds()))
                .header("Authorization", "Bearer " + openRouterProperties.getApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("OpenRouter API returned status code: {}", response.statusCode());
                return new ArrayList<>();
            }

            return parseRecommendationsFromResponse(response.body(), relatedProducts);
        } catch (IOException | InterruptedException e) {
            log.error("Error calling OpenRouter API: {}", e.getMessage());
            throw e;
        }
    }

    private List<RecommendedProductDTO> parseRecommendationsFromResponse(
            String responseBody,
            List<Product> relatedProducts) {

        List<RecommendedProductDTO> recommendations = new ArrayList<>();

        try {
            JsonNode responseJson = objectMapper.readTree(responseBody);
            JsonNode messagesArray = responseJson.get("choices");

            if (messagesArray != null && messagesArray.isArray() && messagesArray.size() > 0) {
                String content = messagesArray.get(0).get("message").get("content").asText();

                // Tìm JSON trong content
                Integer jsonStart = content.indexOf("[");
                Integer jsonEnd = content.lastIndexOf("]");

                if (jsonStart != -1 && jsonEnd != -1) {
                    String jsonString = content.substring(jsonStart, jsonEnd + 1);
                    JsonNode recommendationsArray = objectMapper.readTree(jsonString);

                    for (JsonNode item : recommendationsArray) {
                        Long productId = item.get("productId").asLong();
                        String reason = item.get("reason").asText();

                        // Tìm sản phẩm trong danh sách
                        relatedProducts.stream()
                                .filter(p -> p.getId().equals(productId))
                                .findFirst()
                                .ifPresent(product -> {
                                    recommendations.add(
                                            RecommendedProductDTO.from(product, reason)
                                    );
                                });
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing OpenRouter response: {}", e.getMessage());
        }

        return recommendations;
    }

    private String buildRecommendationPrompt(Product currentProduct, List<Product> relatedProducts) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Tôi đang xem sản phẩm: ").append(currentProduct.getName()).append("\n");
        prompt.append("Đặc tính: CPU: ").append(currentProduct.getCpu())
                .append(", RAM: ").append(currentProduct.getRam())
                .append(", Màn hình: ").append(currentProduct.getScreen())
                .append(", Giá: ").append(currentProduct.getPrice()).append("đ\n\n");

        prompt.append("Danh sách các sản phẩm liên quan (cùng category và brand):\n");

        for (int i = 0; i < relatedProducts.size(); i++) {
            Product p = relatedProducts.get(i);
            prompt.append(i + 1).append(". ID: ").append(p.getId())
                    .append(", Tên: ").append(p.getName())
                    .append(", CPU: ").append(p.getCpu())
                    .append(", RAM: ").append(p.getRam())
                    .append(", Màn hình: ").append(p.getScreen())
                    .append(", Giá: ").append(p.getPrice()).append("đ\n");
        }

        prompt.append("\nDựa trên sản phẩm tôi đang xem, hãy chọn ra 3-5 sản phẩm liên quan tốt nhất từ danh sách trên.\n");
        prompt.append("Trả về kết quả dưới dạng JSON array với cấu trúc: [{\"productId\": <id>, \"reason\": \"lý do ngắn gọn\"}]\n");
        prompt.append("Trả về CHỈ JSON array, không có text khác.\n");

        return prompt.toString();
    }

    private ProductRecommendationResponse fallbackRecommendation(List<Product> relatedProducts) {
        // Fallback: chọn 3-5 sản phẩm đầu tiên
        List<RecommendedProductDTO> recommendations = relatedProducts.stream()
                .limit(5)
                .map(p -> RecommendedProductDTO.from(p, "Sản phẩm liên quan cùng dòng máy"))
                .toList();

        return ProductRecommendationResponse.builder()
                .recommendations(recommendations)
                .aiEnabled(false)
                .message("Gợi ý dựa trên danh sách sản phẩm liên quan")
                .build();
    }

    private boolean hasApiKey() {
        return openRouterProperties.getApiKey() != null &&
                !openRouterProperties.getApiKey().isEmpty() &&
                !openRouterProperties.getApiKey().contains("null");
    }
}
