package com.example.eStore.service;

import com.example.eStore.config.OpenRouterProperties;
import com.example.eStore.dto.ExternalSource;
import com.example.eStore.dto.response.ReviewAiSummaryResponse;
import com.example.eStore.dto.response.ReviewAiSummarySourceResponse;
import com.example.eStore.entity.Product;
import com.example.eStore.entity.ProductReview;
import com.example.eStore.entity.ReviewAiSummary;
import com.example.eStore.repository.ProductRepository;
import com.example.eStore.repository.ProductReviewRepository;
import com.example.eStore.repository.ReviewAiSummaryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewAiSummaryService {
    private static final int EXTERNAL_SOURCE_COUNT = 3;
    private static final int EXTERNAL_CACHE_DAYS = 7;
    private static final String EXTERNAL_SUMMARY_LABEL = "Tóm tắt từ nguồn internet";

    private final ProductRepository productRepository;
    private final ProductReviewRepository productReviewRepository;
    private final ReviewAiSummaryRepository reviewAiSummaryRepository;
    private final OpenRouterProperties openRouterProperties;
    private final SerpApiSearchService serpApiSearchService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public ReviewAiSummaryResponse getReviewSummary(Long productId) throws IOException, InterruptedException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        long reviewCount = productReviewRepository.countByProductId(productId);

        List<ProductReview> latestReviews = new ArrayList<>();
        LocalDateTime latestReviewTime = null;

        if (reviewCount > 0) {
            Page<ProductReview> latestReviewsPage = productReviewRepository.findByProductIdOrderByCreatedAtDesc(
                    productId, PageRequest.of(0, 20));
            latestReviews = latestReviewsPage.getContent();
            if (!latestReviews.isEmpty()) {
                latestReviewTime = latestReviews.get(0).getCreatedAt();
            }
        }

        Optional<ReviewAiSummary> optionalSummary = reviewAiSummaryRepository.findByProductId(productId);
        ReviewAiSummary summaryToUse = optionalSummary.orElseGet(() -> ReviewAiSummary.builder()
                .product(product)
                .build());

        boolean shouldSave = false;
        boolean internalCacheFresh = false;

        if (reviewCount > 0 && !latestReviews.isEmpty() && latestReviewTime != null) {
            internalCacheFresh = isInternalCacheFresh(summaryToUse, latestReviewTime);

            if (!internalCacheFresh && hasApiKey()) {
                ReviewAiSummary generatedSummary = askOpenRouterForSummary(product, latestReviews);
                summaryToUse.setPros(generatedSummary.getPros());
                summaryToUse.setCons(generatedSummary.getCons());
                summaryToUse.setSummary(generatedSummary.getSummary());
                summaryToUse.setReviewCount((int) reviewCount);
                summaryToUse.setLastGeneratedAt(LocalDateTime.now());
                shouldSave = true;
            }
        }

        try {
            boolean externalUpdated = refreshExternalSummary(summaryToUse, product);
            if (externalUpdated) {
                shouldSave = true;
            }
        } catch (Exception e) {
            log.error("Failed to generate external summary for product {}: {}", productId, e.getMessage());
            if (summaryToUse.getExternalSummary() == null) {
                summaryToUse.setExternalSummary("Không thể tạo tóm tắt từ nguồn internet.");
                summaryToUse.setExternalSources("[]");
                summaryToUse.setExternalGeneratedAt(LocalDateTime.now());
                shouldSave = true;
            }
        }

        if (shouldSave) {
            summaryToUse = reviewAiSummaryRepository.save(summaryToUse);
        }

        ReviewAiSummaryResponse response = mapToResponse(product, summaryToUse);
        response.setReviewCount((int) reviewCount);

        if (reviewCount == 0 || latestReviews.isEmpty()) {
            response.setSummary("Sản phẩm chưa có đánh giá nào.");
            response.setPros(new ArrayList<>());
            response.setCons(new ArrayList<>());
            response.setLastGeneratedAt(LocalDateTime.now());
        } else if (!internalCacheFresh && !hasApiKey()) {
            response.setSummary("Tính năng tóm tắt AI hiện không khả dụng do thiếu cấu hình.");
            response.setPros(new ArrayList<>());
            response.setCons(new ArrayList<>());
            response.setLastGeneratedAt(LocalDateTime.now());
        }

        if (response.getExternalSummary() == null) {
            response.setExternalSummary("Không có dữ liệu tóm tắt từ internet.");
            response.setExternalSources(new ArrayList<>());
        }

        return response;
    }

    private boolean isInternalCacheFresh(ReviewAiSummary summary, LocalDateTime latestReviewTime) {
        return summary.getLastGeneratedAt() != null
                && (summary.getLastGeneratedAt().isAfter(latestReviewTime)
                || summary.getLastGeneratedAt().isEqual(latestReviewTime));
    }

    private boolean shouldRefreshExternalSummary(ReviewAiSummary summary) {
        if (summary.getExternalGeneratedAt() == null) {
            return true;
        }
        return summary.getExternalGeneratedAt().isBefore(LocalDateTime.now().minusDays(EXTERNAL_CACHE_DAYS));
    }

    private boolean refreshExternalSummary(ReviewAiSummary summary, Product product) throws IOException, InterruptedException {
        if (!shouldRefreshExternalSummary(summary)) {
            return false;
        }

        if (!serpApiSearchService.hasApiKey()) {
            if (summary.getExternalSummary() == null) {
                summary.setExternalSummary("Tính năng tóm tắt từ internet hiện không khả dụng do thiếu cấu hình.");
                summary.setExternalSources("[]");
                summary.setExternalGeneratedAt(LocalDateTime.now());
                return true;
            }
            return false;
        }

        List<ExternalSource> sources = serpApiSearchService.searchProductSources(product.getName(), EXTERNAL_SOURCE_COUNT);
        List<ReviewAiSummarySourceResponse> responseSources = mapExternalSources(sources);
        summary.setExternalSources(objectMapper.writeValueAsString(responseSources));
        summary.setExternalGeneratedAt(LocalDateTime.now());

        if (sources.isEmpty()) {
            summary.setExternalSummary("Không tìm thấy nguồn internet phù hợp.");
            return true;
        }

        if (!hasApiKey()) {
            summary.setExternalSummary("Tính năng tóm tắt từ internet hiện không khả dụng do thiếu cấu hình.");
            return true;
        }

        ReviewAiSummary externalSummary = askOpenRouterForExternalSummary(product, sources);
        summary.setExternalSummary(externalSummary.getSummary());
        return true;
    }

    private List<ReviewAiSummarySourceResponse> mapExternalSources(List<ExternalSource> sources) {
        List<ReviewAiSummarySourceResponse> responseSources = new ArrayList<>();
        for (ExternalSource source : sources) {
            responseSources.add(ReviewAiSummarySourceResponse.builder()
                    .title(source.getTitle())
                    .url(source.getUrl())
                    .website(source.getWebsite())
                    .build());
        }
        return responseSources;
    }

    private ReviewAiSummary askOpenRouterForSummary(Product product, List<ProductReview> reviews) throws IOException, InterruptedException {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Bạn là một chuyên gia phân tích đánh giá sản phẩm e-commerce. ");
        prompt.append("Hãy tóm tắt các đánh giá sau đây của sản phẩm '").append(product.getName()).append("'.\n\n");

        prompt.append("Danh sách đánh giá:\n");
        for (int i = 0; i < reviews.size(); i++) {
            ProductReview r = reviews.get(i);
            prompt.append("- Rating: ").append(r.getRating()).append("/5 sao. ");
            if (r.getComment() != null && !r.getComment().trim().isEmpty()) {
                prompt.append("Comment: ").append(r.getComment());
            }
            prompt.append("\n");
        }

        prompt.append("\nYêu cầu đầu ra:\n");
        prompt.append("1. Phân tích ưu điểm (pros) và nhược điểm (cons) dựa trên nội dung.\n");
        prompt.append("2. Trả về đúng định dạng JSON chuẩn (không có markdown code block như ```json). Định dạng như sau:\n");
        prompt.append("{\n");
        prompt.append("  \"pros\": [\"ưu điểm 1\", \"ưu điểm 2\"],\n");
        prompt.append("  \"cons\": [\"nhược điểm 1\", \"nhược điểm 2\"],\n");
        prompt.append("  \"summary\": \"Đánh giá tổng quan (tối đa 2-3 câu)\"\n");
        prompt.append("}\n");
        prompt.append("Lưu ý: Chỉ trả về JSON, không thêm bất kỳ văn bản nào khác. Nếu số lượng đánh giá ít hoặc thiếu thông tin, hãy ghi rõ trong summary.");

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", openRouterProperties.getModel());
        requestBody.put("max_tokens", 800);
        requestBody.put("temperature", 0.5);
        requestBody.put("messages", List.of(
                Map.of(
                        "role", "user",
                        "content", prompt.toString()
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

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.warn("OpenRouter API returned status code: {} - {}", response.statusCode(), response.body());
            throw new RuntimeException("OpenRouter API call failed");
        }

        return parseSummaryFromResponse(response.body());
    }

    private ReviewAiSummary askOpenRouterForExternalSummary(Product product, List<ExternalSource> sources) throws IOException, InterruptedException {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Bạn là một chuyên gia tổng hợp đánh giá từ các nguồn internet. ");
        prompt.append("Hãy tạo 'Tóm tắt từ nguồn internet' cho sản phẩm '").append(product.getName()).append("'.\n\n");
        prompt.append("Danh sách nguồn (tiêu đề, website, trích đoạn, url):\n");

        for (ExternalSource source : sources) {
            prompt.append("- ").append(source.getTitle());
            if (source.getWebsite() != null && !source.getWebsite().isEmpty()) {
                prompt.append(" (").append(source.getWebsite()).append(")");
            }
            if (source.getSnippet() != null && !source.getSnippet().isEmpty()) {
                prompt.append(". Trích đoạn: ").append(source.getSnippet());
            }
            prompt.append(". Url: ").append(source.getUrl()).append("\n");
        }

        prompt.append("\nYêu cầu đầu ra:\n");
        prompt.append("1. Tóm tắt ngắn gọn đánh giá từ các nguồn internet (2-3 câu).\n");
        prompt.append("2. Trả về đúng định dạng JSON chuẩn, không có markdown. Định dạng:\n");
        prompt.append("{\n");
        prompt.append("  \"summary\": \"Tóm tắt đánh giá từ internet\"\n");
        prompt.append("}\n");
        prompt.append("Lưu ý: Chỉ trả về JSON, không thêm bất kỳ văn bản nào khác.");

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", openRouterProperties.getModel());
        requestBody.put("max_tokens", 600);
        requestBody.put("temperature", 0.4);
        requestBody.put("messages", List.of(
                Map.of(
                        "role", "user",
                        "content", prompt.toString()
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

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.warn("OpenRouter API returned status code: {} - {}", response.statusCode(), response.body());
            throw new RuntimeException("OpenRouter API call failed");
        }

        return parseExternalSummaryFromResponse(response.body());
    }

    private ReviewAiSummary parseSummaryFromResponse(String responseBody) throws JsonProcessingException {
        JsonNode responseJson = objectMapper.readTree(responseBody);
        JsonNode messagesArray = responseJson.get("choices");

        if (messagesArray != null && messagesArray.isArray() && !messagesArray.isEmpty()) {
            String content = messagesArray.get(0).get("message").get("content").asText();

            int jsonStart = content.indexOf("{");
            int jsonEnd = content.lastIndexOf("}");

            if (jsonStart != -1 && jsonEnd != -1) {
                String jsonString = content.substring(jsonStart, jsonEnd + 1);

                JsonNode summaryJson = objectMapper.readTree(jsonString);

                String prosStr = "[]";
                String consStr = "[]";

                if (summaryJson.has("pros")) {
                    prosStr = objectMapper.writeValueAsString(summaryJson.get("pros"));
                }
                if (summaryJson.has("cons")) {
                    consStr = objectMapper.writeValueAsString(summaryJson.get("cons"));
                }

                String summaryText = summaryJson.has("summary") ? summaryJson.get("summary").asText() : "Không thể tạo tóm tắt.";

                ReviewAiSummary result = new ReviewAiSummary();
                result.setPros(prosStr);
                result.setCons(consStr);
                result.setSummary(summaryText);
                return result;
            }
        }

        throw new RuntimeException("Cannot parse JSON from OpenRouter response");
    }

    private ReviewAiSummary parseExternalSummaryFromResponse(String responseBody) throws JsonProcessingException {
        JsonNode responseJson = objectMapper.readTree(responseBody);
        JsonNode messagesArray = responseJson.get("choices");

        if (messagesArray != null && messagesArray.isArray() && !messagesArray.isEmpty()) {
            String content = messagesArray.get(0).get("message").get("content").asText();

            int jsonStart = content.indexOf("{");
            int jsonEnd = content.lastIndexOf("}");

            if (jsonStart != -1 && jsonEnd != -1) {
                String jsonString = content.substring(jsonStart, jsonEnd + 1);
                JsonNode summaryJson = objectMapper.readTree(jsonString);
                String summaryText = summaryJson.has("summary") ? summaryJson.get("summary").asText() : "Không thể tạo tóm tắt.";
                ReviewAiSummary result = new ReviewAiSummary();
                result.setSummary(summaryText);
                return result;
            }
        }

        throw new RuntimeException("Cannot parse JSON from OpenRouter response");
    }

    private boolean hasApiKey() {
        return openRouterProperties.getApiKey() != null &&
                !openRouterProperties.getApiKey().isEmpty() &&
                !openRouterProperties.getApiKey().contains("null");
    }

    private ReviewAiSummaryResponse mapToResponse(Product product, ReviewAiSummary summary) {
        List<String> prosList = new ArrayList<>();
        List<String> consList = new ArrayList<>();
        List<ReviewAiSummarySourceResponse> externalSources = new ArrayList<>();

        try {
            if (summary.getPros() != null) {
                prosList = objectMapper.readValue(summary.getPros(), new TypeReference<List<String>>() {});
            }
            if (summary.getCons() != null) {
                consList = objectMapper.readValue(summary.getCons(), new TypeReference<List<String>>() {});
            }
            if (summary.getExternalSources() != null) {
                externalSources = objectMapper.readValue(summary.getExternalSources(), new TypeReference<List<ReviewAiSummarySourceResponse>>() {});
            }
        } catch (Exception e) {
            log.warn("Failed to parse summary json for product {}: {}", product.getId(), e.getMessage());
        }

        return ReviewAiSummaryResponse.builder()
                .productId(product.getId())
                .pros(prosList)
                .cons(consList)
                .summary(summary.getSummary())
                .reviewCount(summary.getReviewCount())
                .lastGeneratedAt(summary.getLastGeneratedAt())
                .externalSummaryLabel(EXTERNAL_SUMMARY_LABEL)
                .externalSummary(summary.getExternalSummary())
                .externalSources(externalSources)
                .externalSummaryGeneratedAt(summary.getExternalGeneratedAt())
                .build();
    }

    private ReviewAiSummaryResponse createEmptyResponse(Long productId) {
        return ReviewAiSummaryResponse.builder()
                .productId(productId)
                .pros(new ArrayList<>())
                .cons(new ArrayList<>())
                .summary("Không đủ dữ liệu đánh giá để tạo tóm tắt.")
                .reviewCount(0)
                .lastGeneratedAt(LocalDateTime.now())
                .externalSummaryLabel(EXTERNAL_SUMMARY_LABEL)
                .externalSummary("Không có dữ liệu tóm tắt từ internet.")
                .externalSources(new ArrayList<>())
                .externalSummaryGeneratedAt(LocalDateTime.now())
                .build();
    }
}
