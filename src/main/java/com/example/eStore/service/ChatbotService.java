package com.example.eStore.service;

import com.example.eStore.config.OpenAiProperties;
import com.example.eStore.dto.request.ChatIntent;
import com.example.eStore.dto.request.ChatbotHistoryMessage;
import com.example.eStore.dto.request.ChatbotRequest;
import com.example.eStore.dto.response.ChatbotProductSuggestionResponse;
import com.example.eStore.dto.response.ChatbotResponse;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatbotService {
    private final ProductRepository productRepository;
    private final OpenAiProperties openAiProperties;
    private final IntentParser intentParser;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public ChatbotResponse chat(ChatbotRequest request) {
        String message = request.getMessage().trim();
        ChatIntent intent = intentParser.parseIntentWithAI(message);

        int max = Math.max(1, openAiProperties.getMaxCatalogProducts());
        List<Product> products = productRepository.search(
                buildSearchKeyword(intent),
                intent.getCategory(),
                intent.getMinPrice(),
                intent.getMaxPrice(),
                PageRequest.of(0, max)
        );

        if (!hasApiKey()) {
            return fallbackResponse(products, false);
        }

        try {
            ModelOutput modelOutput = askOpenAi(message, intent, request.getHistory(), products);
            List<ChatbotProductSuggestionResponse> suggestions = suggestionsFromModel(modelOutput.productIds(), products);
            if (suggestions.isEmpty()) {
                suggestions = products.stream().limit(3).map(this::toSuggestion).toList();
            }

            String answer = modelOutput.answer();
            if (!isBlank(modelOutput.followUpQuestion()) && !answer.contains(modelOutput.followUpQuestion())) {
                answer = answer + "\n\n" + modelOutput.followUpQuestion();
            }

            return ChatbotResponse.builder()
                    .answer(answer)
                    .suggestions(suggestions)
                    .aiEnabled(true)
                    .build();
        } catch (Exception exception) {
            log.warn("OpenAI chatbot request failed, using local fallback: {}", exception.getMessage());
            return fallbackResponse(products, false);
        }
    }

    private ModelOutput askOpenAi(String message, ChatIntent intent, List<ChatbotHistoryMessage> history, List<Product> products)
            throws IOException, InterruptedException {

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", openAiProperties.getModel());
        requestBody.put("instructions", buildInstructions());
        requestBody.put("input", buildUserInput(message, intent, history, products));
        requestBody.put("max_output_tokens", openAiProperties.getMaxOutputTokens());
        requestBody.put("text", Map.of("format", buildResponseFormat()));

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(openAiProperties.getTimeoutSeconds()))
                .build();

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(openAiProperties.getResponsesUrl()))
                .timeout(Duration.ofSeconds(openAiProperties.getTimeoutSeconds()))
                .header("Authorization", "Bearer " + openAiProperties.getApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("OpenAI returned HTTP " + response.statusCode() + ": " + extractErrorMessage(response.body()));
        }

        String outputText = extractOutputText(response.body());
        JsonNode node = objectMapper.readTree(outputText);

        List<Long> productIds = new ArrayList<>();
        JsonNode idsNode = node.path("recommendedProductIds");
        if (idsNode.isArray()) {
            idsNode.forEach(idNode -> {
                if (idNode.canConvertToLong()) {
                    productIds.add(idNode.asLong());
                }
            });
        }

        return new ModelOutput(
                node.path("answer").asText("Mình đã tìm thấy một vài sản phẩm phù hợp."),
                productIds,
                node.path("followUpQuestion").asText("")
        );
    }

    private String buildInstructions() {
        return """
                You are an AI sales assistant for E-Store, an electronics ecommerce website.
                Reply in Vietnamese with a natural, concise, helpful tone.
                You will receive a parsed intent object and a filtered product catalog.
                Only recommend products that exist in PRODUCT_CATALOG and reference them by id in recommendedProductIds.
                Do not invent prices, discounts, stock status, brands, categories, or product names.
                Use the parsed intent, the user's budget, product type, and purpose to compare options.
                If the catalogue has no exact fit, explain the closest alternatives and ask one short follow-up question.
                Return JSON only, matching the supplied schema.
                """;
    }

    private String buildUserInput(String message, ChatIntent intent, List<ChatbotHistoryMessage> history, List<Product> products)
            throws IOException {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("customerMessage", message);
        payload.put("intent", intent);
        payload.put("recentHistory", sanitizeHistory(history));
        payload.put("productCatalog", products.stream().map(this::toCatalogItem).toList());
        return objectMapper.writeValueAsString(payload);
    }

    private List<Map<String, String>> sanitizeHistory(List<ChatbotHistoryMessage> history) {
        if (history == null || history.isEmpty()) {
            return List.of();
        }

        return history.stream()
                .filter(Objects::nonNull)
                .filter(item -> !isBlank(item.getContent()))
                .skip(Math.max(0, history.size() - 8))
                .map(item -> Map.of(
                        "role", normalizeRole(item.getRole()),
                        "content", item.getContent().trim()
                ))
                .toList();
    }

    private String normalizeRole(String role) {
        if ("assistant".equalsIgnoreCase(role)) {
            return "assistant";
        }
        return "user";
    }

    private Map<String, Object> buildResponseFormat() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("additionalProperties", false);
        schema.put("required", List.of("answer", "recommendedProductIds", "followUpQuestion"));
        schema.put("properties", Map.of(
                "answer", Map.of(
                        "type", "string",
                        "description", "Vietnamese answer shown to the customer."
                ),
                "recommendedProductIds", Map.of(
                        "type", "array",
                        "description", "IDs from PRODUCT_CATALOG that best match the customer's need.",
                        "items", Map.of("type", "integer")
                ),
                "followUpQuestion", Map.of(
                        "type", "string",
                        "description", "A short Vietnamese follow-up question, or an empty string if not needed."
                )
        ));

        Map<String, Object> format = new LinkedHashMap<>();
        format.put("type", "json_schema");
        format.put("name", "estore_product_advice");
        format.put("strict", true);
        format.put("schema", schema);
        return format;
    }

    private String extractOutputText(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        StringBuilder builder = new StringBuilder();

        JsonNode output = root.path("output");
        if (output.isArray()) {
            for (JsonNode item : output) {
                JsonNode content = item.path("content");
                if (!content.isArray()) {
                    continue;
                }
                for (JsonNode part : content) {
                    JsonNode text = part.path("text");
                    if (!text.isMissingNode()) {
                        builder.append(text.asText());
                    }
                }
            }
        }

        if (builder.isEmpty() && root.hasNonNull("output_text")) {
            builder.append(root.path("output_text").asText());
        }

        if (builder.isEmpty()) {
            throw new IllegalStateException("OpenAI response did not contain text output");
        }

        return builder.toString();
    }

    private String extractErrorMessage(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("error").path("message").asText(responseBody);
        } catch (Exception ignored) {
            return responseBody;
        }
    }

    private String buildSearchKeyword(ChatIntent intent) {
        if (intent == null) {
            return null;
        }
        if (intent.getProductNames() != null && !intent.getProductNames().isEmpty()) {
            return intent.getProductNames().get(0);
        }
        if (!isBlank(intent.getUsage())) {
            return intent.getUsage();
        }
        if (!isBlank(intent.getCategory())) {
            return intent.getCategory();
        }
        return null;
    }

    private List<ChatbotProductSuggestionResponse> suggestionsFromModel(List<Long> productIds, List<Product> products) {
        Map<Long, Product> byId = products.stream().collect(Collectors.toMap(Product::getId, product -> product));
        return productIds.stream()
                .distinct()
                .map(byId::get)
                .filter(Objects::nonNull)
                .limit(4)
                .map(this::toSuggestion)
                .toList();
    }

    private ChatbotResponse fallbackResponse(List<Product> products, boolean aiEnabled) {
        List<ChatbotProductSuggestionResponse> suggestions = products.stream()
                .limit(3)
                .map(this::toSuggestion)
                .toList();

        String answer = suggestions.isEmpty()
                ? "Mình chưa tìm thấy sản phẩm phù hợp trong catalogue hiện tại. Bạn có thể nói rõ loại thiết bị, ngân sách và nhu cầu sử dụng để mình lọc lại."
                : "Mình tạm lọc được một vài sản phẩm gần với nhu cầu. Kết quả như sau: ";

        return ChatbotResponse.builder()
                .answer(answer)
                .suggestions(suggestions)
                .aiEnabled(aiEnabled)
                .build();
    }

    private ChatbotProductSuggestionResponse toSuggestion(Product product) {
        return ChatbotProductSuggestionResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .thumbnailUrl(product.getThumbnailUrl())
                .cpu(product.getCpu())
                .ram(product.getRam())
                .stockQuantity(product.getStockQuantity())
                .build();
    }

    private Map<String, Object> toCatalogItem(Product product) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", product.getId());
        item.put("name", product.getName());
        item.put("priceVnd", product.getPrice());
        item.put("category", product.getCategory() != null ? product.getCategory().getName() : "");
        item.put("brand", product.getBrand() != null ? product.getBrand().getName() : "");
        item.put("cpu", product.getCpu());
        item.put("ram", product.getRam());
        item.put("screen", product.getScreen());
        item.put("operatingSystem", product.getOperatingSystem());
        item.put("batteryCapacity", product.getBatteryCapacity());
        item.put("design", product.getDesign());
        item.put("stockQuantity", product.getStockQuantity());
        item.put("soldQuantity", product.getSoldQuantity());
        item.put("description", abbreviate(product.getDescription(), 260));
        return item;
    }

    private boolean hasApiKey() {
        return !isBlank(openAiProperties.getApiKey());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String abbreviate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }

    private record ModelOutput(String answer, List<Long> productIds, String followUpQuestion) {
    }
}
