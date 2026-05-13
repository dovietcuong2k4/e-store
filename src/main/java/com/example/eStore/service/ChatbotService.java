package com.example.eStore.service;

import com.example.eStore.config.OpenRouterProperties;
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
    private final OpenRouterProperties openRouterProperties;
    private final IntentParser intentParser;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public ChatbotResponse chat(ChatbotRequest request) {
        String message = request.getMessage().trim();
        ChatIntent intent = intentParser.parseIntentWithAI(message);

        return switch (intent.getIntent()) {
            case "compare" -> handleCompare(message, intent, request.getHistory());
            case "product_info" -> handleProductInfo(message, intent, request.getHistory());
            case "faq" -> handleFaq(message, intent, request.getHistory());
            case "unknown" -> handleUnknown(intent);
            default -> handleRecommend(message, intent, request.getHistory());
        };
    }

    // ======================== Intent Handlers ========================

    private ChatbotResponse handleRecommend(String message, ChatIntent intent, List<ChatbotHistoryMessage> history) {
        List<Product> products = searchProducts(intent);

        if (!hasApiKey()) {
            return fallbackResponse(products, false, "recommend");
        }

        try {
            ModelOutput output = askOpenRouter(message, intent, history, products, buildRecommendInstructions());
            return buildProductResponse(output, products, "recommend");
        } catch (Exception exception) {
            log.warn("OpenRouter recommend request failed, using fallback: {}", exception.getMessage());
            return fallbackResponse(products, false, "recommend");
        }
    }

    private ChatbotResponse handleCompare(String message, ChatIntent intent, List<ChatbotHistoryMessage> history) {
        List<Product> products = searchProducts(intent);

        if (!hasApiKey()) {
            return fallbackResponse(products, false, "compare");
        }

        try {
            ModelOutput output = askOpenRouter(message, intent, history, products, buildCompareInstructions());
            return buildProductResponse(output, products, "compare");
        } catch (Exception exception) {
            log.warn("OpenRouter compare request failed, using fallback: {}", exception.getMessage());
            return fallbackResponse(products, false, "compare");
        }
    }

    private ChatbotResponse handleProductInfo(String message, ChatIntent intent, List<ChatbotHistoryMessage> history) {
        List<Product> products = searchProducts(intent);

        if (!hasApiKey()) {
            return fallbackResponse(products, false, "product_info");
        }

        try {
            ModelOutput output = askOpenRouter(message, intent, history, products, buildProductInfoInstructions());
            return buildProductResponse(output, products, "product_info");
        } catch (Exception exception) {
            log.warn("OpenRouter product_info request failed, using fallback: {}", exception.getMessage());
            return fallbackResponse(products, false, "product_info");
        }
    }

    private ChatbotResponse handleFaq(String message, ChatIntent intent, List<ChatbotHistoryMessage> history) {
        if (!hasApiKey()) {
            return ChatbotResponse.builder()
                    .answer("Cảm ơn bạn đã liên hệ! Hiện tại mình chưa thể trả lời câu hỏi này tự động. "
                            + "Vui lòng liên hệ hotline hoặc email hỗ trợ để được tư vấn chi tiết hơn.")
                    .suggestions(List.of())
                    .aiEnabled(false)
                    .intent("faq")
                    .responseType("text_only")
                    .build();
        }

        try {
            ModelOutput output = askOpenRouterFaq(message, intent, history);
            String answer = output.answer();
            if (!isBlank(output.followUpQuestion()) && !answer.contains(output.followUpQuestion())) {
                answer = answer + "\n\n" + output.followUpQuestion();
            }

            return ChatbotResponse.builder()
                    .answer(answer)
                    .suggestions(List.of())
                    .aiEnabled(true)
                    .intent("faq")
                    .responseType("text_only")
                    .build();
        } catch (Exception exception) {
            log.warn("OpenRouter FAQ request failed: {}", exception.getMessage());
            return ChatbotResponse.builder()
                    .answer("Cảm ơn bạn đã liên hệ! Hiện tại mình chưa thể trả lời câu hỏi này tự động. "
                            + "Vui lòng liên hệ hotline hoặc email hỗ trợ để được tư vấn chi tiết hơn.")
                    .suggestions(List.of())
                    .aiEnabled(false)
                    .intent("faq")
                    .responseType("text_only")
                    .build();
        }
    }

    private ChatbotResponse handleUnknown(ChatIntent intent) {
        String answer = """
                Mình chưa hiểu rõ yêu cầu của bạn. Bạn có thể thử hỏi theo các cách sau:
                • **Tư vấn sản phẩm**: "Tư vấn laptop gaming dưới 20 triệu"
                • **So sánh sản phẩm**: "So sánh iPhone 15 với Samsung S24"
                • **Thông tin sản phẩm**: "Thông tin chi tiết MacBook Air M2"
                • **Câu hỏi chung**: "Chính sách bảo hành của cửa hàng"
                """;

        return ChatbotResponse.builder()
                .answer(answer)
                .suggestions(List.of())
                .aiEnabled(false)
                .intent("unknown")
                .responseType("guide")
                .build();
    }

    // ======================== OpenRouter Calls ========================

    /**
     * OpenRouter call for product-related intents (recommend, compare, product_info).
     * Sends product catalog in the input and expects recommendedProductIds in the response.
     */
    private ModelOutput askOpenRouter(String message, ChatIntent intent, List<ChatbotHistoryMessage> history,
                                  List<Product> products, String instructions)
            throws IOException, InterruptedException {

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", openRouterProperties.getModel());
        requestBody.put("instructions", instructions);
        requestBody.put("input", buildUserInput(message, intent, history, products));
        requestBody.put("max_output_tokens", openRouterProperties.getMaxOutputTokens());
        requestBody.put("text", Map.of("format", buildProductResponseFormat()));

        String responseBody = callOpenRouter(requestBody);
        String outputText = extractOutputText(responseBody);
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
                node.path("followUpQuestion").asText(""),
                node.path("responseType").asText("")
        );
    }

    /**
     * OpenRouter call for FAQ intent.
     * No product catalog is sent. Response schema only has answer + followUpQuestion.
     */
    private ModelOutput askOpenRouterFaq(String message, ChatIntent intent, List<ChatbotHistoryMessage> history)
            throws IOException, InterruptedException {

        Map<String, Object> faqInput = new LinkedHashMap<>();
        faqInput.put("customerMessage", message);
        faqInput.put("intent", intent);
        faqInput.put("recentHistory", sanitizeHistory(history));

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", openRouterProperties.getModel());
        requestBody.put("instructions", buildFaqInstructions());
        requestBody.put("input", objectMapper.writeValueAsString(faqInput));
        requestBody.put("max_output_tokens", openRouterProperties.getMaxOutputTokens());
        requestBody.put("text", Map.of("format", buildFaqResponseFormat()));

        String responseBody = callOpenRouter(requestBody);
        String outputText = extractOutputText(responseBody);
        JsonNode node = objectMapper.readTree(outputText);

        return new ModelOutput(
                node.path("answer").asText("Cảm ơn bạn đã liên hệ! Mình sẽ giải đáp ngay."),
                List.of(),
                node.path("followUpQuestion").asText(""),
                node.path("responseType").asText("text_only")
        );
    }

    private String callOpenRouter(Map<String, Object> requestBody) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(openRouterProperties.getTimeoutSeconds()))
                .build();

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(openRouterProperties.getResponsesUrl()))
                .timeout(Duration.ofSeconds(openRouterProperties.getTimeoutSeconds()))
                .header("Authorization", "Bearer " + openRouterProperties.getApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("OpenRouter returned HTTP " + response.statusCode() + ": " + extractErrorMessage(response.body()));
        }

        return response.body();
    }

    // ======================== System Prompts (Instructions) ========================

    private String buildRecommendInstructions() {
        return """
                You are an AI sales assistant for E-Store, an electronics ecommerce website.
                Reply in Vietnamese with a warm, consultative tone — like a knowledgeable friend helping the customer shop.

                CONTEXT:
                - You will receive a parsed intent object and a filtered product catalog (PRODUCT_CATALOG).
                - Use the customer's budget, product type, purpose, and brand preference to recommend the best options.

                ANSWER GUIDELINES:
                - Write your answer naturally using Markdown for better readability.
                - Use **bold text** to highlight product names or key features.
                - Use bullet points or numbered lists to present multiple options.
                - Highlight key strengths of each recommended product and explain why it fits the customer's needs.
                - If the catalog has no exact fit, explain the closest alternatives honestly.
                - You may suggest similar or alternative products from the catalog if they could also be a good fit.

                STRICT RULES:
                - Only recommend products that exist in PRODUCT_CATALOG. Reference them by id in recommendedProductIds.
                - Do NOT invent prices, discounts, stock status, brands, categories, specs, or product names.
                - Respect the parsed brand when the customer asks for a specific brand.
                - Set responseType to "product_recommendation".
                - Return JSON only, matching the supplied schema.
                """;
    }

    private String buildCompareInstructions() {
        return """
                You are a product comparison specialist for E-Store, an electronics ecommerce website.
                Reply in Vietnamese with a clear, objective tone.

                CONTEXT:
                - You will receive a parsed intent object and a filtered product catalog (PRODUCT_CATALOG).
                - The customer is asking to compare products or wants to know which product is better / more suitable.

                ANSWER GUIDELINES:
                - Directly answer the customer's comparison question using Markdown formatting.
                - Use **Markdown tables** or **bullet lists** to compare specs, prices, and features side-by-side.
                - Use # or ## headers for different sections if the comparison is lengthy.
                - Identify the relevant products from PRODUCT_CATALOG and compare them based on key specs, price, and use case.
                - Provide a clear verdict or recommendation based on the customer's stated needs or budget.
                - If the catalog contains similar or alternative products that the customer may also want to consider, briefly mention them.
                - If only one matching product is found, describe it thoroughly and suggest similar alternatives from the catalog for comparison.

                STRICT RULES:
                - Only compare products that exist in PRODUCT_CATALOG. Reference them by id in recommendedProductIds.
                - Do NOT invent prices, discounts, stock status, brands, categories, specs, or product names.
                - Set responseType to "product_comparison".
                - Return JSON only, matching the supplied schema.
                """;
    }

    private String buildProductInfoInstructions() {
        return """
                You are a product details expert for E-Store, an electronics ecommerce website.
                Reply in Vietnamese with detailed, accurate information.

                CONTEXT:
                - You will receive a parsed intent object and a filtered product catalog (PRODUCT_CATALOG).
                - The customer wants detailed information about a specific product.

                ANSWER GUIDELINES:
                - Provide comprehensive details about the most relevant product using Markdown.
                - Use **bold text** for the product name and key specs.
                - Use bullet points for technical specifications (CPU, RAM, Screen, etc.).
                - Structure your answer naturally — adapt the format and depth to match the customer's question.
                - If the product has low or zero stock, proactively suggest similar alternatives from the catalog.
                - If the exact product is not found, inform the customer and suggest the closest alternatives available.

                STRICT RULES:
                - Only describe products that exist in PRODUCT_CATALOG. Reference them by id in recommendedProductIds.
                - Do NOT invent prices, discounts, stock status, brands, categories, specs, or product names.
                - Set responseType to "product_detail".
                - Return JSON only, matching the supplied schema.
                """;
    }

    private String buildFaqInstructions() {
        return """
                You are a customer support agent for E-Store, an electronics ecommerce website.
                Reply in Vietnamese with a friendly, professional, and reassuring tone.
                Answer frequently asked questions about the store's policies and services.
                Use **Markdown** (headers, bold, lists) to make the information easy to scan.
                Cover topics such as:
                - Warranty policy (bảo hành)
                - Return and refund policy (đổi trả, hoàn tiền)
                - Shipping and delivery (giao hàng, vận chuyển)
                - Payment methods (thanh toán)
                - Order tracking (theo dõi đơn hàng)
                - Store contact information and working hours
                If you are not sure about specific policy details, provide general best-practice information
                and advise the customer to contact support for exact details.
                Do NOT recommend or mention any products. Do NOT return any product IDs.
                Set responseType to "text_only".
                Return JSON only, matching the supplied schema.
                """;
    }

    // ======================== Response Formats ========================

    /**
     * JSON schema for product-related intents (recommend, compare, product_info).
     */
    private Map<String, Object> buildProductResponseFormat() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("additionalProperties", false);
        schema.put("required", List.of("answer", "recommendedProductIds", "followUpQuestion", "responseType"));
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
                ),
                "responseType", Map.of(
                        "type", "string",
                        "enum", List.of("product_recommendation", "product_comparison", "product_detail"),
                        "description", "How the frontend should render this response."
                )
        ));

        Map<String, Object> format = new LinkedHashMap<>();
        format.put("type", "json_schema");
        format.put("name", "estore_product_advice");
        format.put("strict", true);
        format.put("schema", schema);
        return format;
    }

    /**
     * JSON schema for FAQ intent — no product IDs needed.
     */
    private Map<String, Object> buildFaqResponseFormat() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("additionalProperties", false);
        schema.put("required", List.of("answer", "followUpQuestion", "responseType"));
        schema.put("properties", Map.of(
                "answer", Map.of(
                        "type", "string",
                        "description", "Vietnamese answer to the customer's FAQ question."
                ),
                "followUpQuestion", Map.of(
                        "type", "string",
                        "description", "A short Vietnamese follow-up question, or an empty string if not needed."
                ),
                "responseType", Map.of(
                        "type", "string",
                        "enum", List.of("text_only"),
                        "description", "Always text_only for FAQ responses."
                )
        ));

        Map<String, Object> format = new LinkedHashMap<>();
        format.put("type", "json_schema");
        format.put("name", "estore_faq_answer");
        format.put("strict", true);
        format.put("schema", schema);
        return format;
    }

    // ======================== Helpers ========================

    private List<Product> searchProducts(ChatIntent intent) {
        int max = Math.max(1, openRouterProperties.getMaxCatalogProducts());
        return productRepository.search(
                buildSearchKeyword(intent),
                intent.getCategory(),
                intent.getBrand(),
                intent.getMinPrice(),
                intent.getMaxPrice(),
                PageRequest.of(0, max)
        );
    }

    private ChatbotResponse buildProductResponse(ModelOutput output, List<Product> products, String intentType) {
        List<ChatbotProductSuggestionResponse> suggestions = suggestionsFromModel(output.productIds(), products);

        String answer = output.answer();
        if (!isBlank(output.followUpQuestion()) && !answer.contains(output.followUpQuestion())) {
            answer = answer + "\n\n" + output.followUpQuestion();
        }

        String responseType = isBlank(output.responseType())
                ? resolveResponseType(intentType)
                : output.responseType();

        return ChatbotResponse.builder()
                .answer(answer)
                .suggestions(suggestions)
                .aiEnabled(true)
                .intent(intentType)
                .responseType(responseType)
                .build();
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
            throw new IllegalStateException("OpenRouter response did not contain text output");
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
        if (!isBlank(intent.getBrand())) {
            return intent.getBrand();
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

    private ChatbotResponse fallbackResponse(List<Product> products, boolean aiEnabled, String intentType) {
        List<ChatbotProductSuggestionResponse> suggestions = products.stream()
                .limit(3)
                .map(this::toSuggestion)
                .toList();

        String answer = switch (intentType) {
            case "compare" -> suggestions.isEmpty()
                    ? "Mình chưa tìm thấy sản phẩm để so sánh trong catalogue hiện tại. Bạn có thể nói rõ tên 2 sản phẩm bạn muốn so sánh."
                    : "Mình tạm lọc được một vài sản phẩm liên quan. Bạn muốn so sánh những sản phẩm nào trong danh sách dưới đây?";
            case "product_info" -> suggestions.isEmpty()
                    ? "Mình chưa tìm thấy sản phẩm bạn hỏi trong catalogue hiện tại. Bạn có thể nói rõ tên sản phẩm cần xem thông tin."
                    : "Mình đã tìm thấy sản phẩm liên quan. Dưới đây là kết quả:";
            default -> suggestions.isEmpty()
                    ? "Mình chưa tìm thấy sản phẩm phù hợp trong catalogue hiện tại. Bạn có thể nói rõ loại thiết bị, ngân sách và nhu cầu sử dụng để mình lọc lại."
                    : "Mình tạm lọc được một vài sản phẩm gần với nhu cầu. Kết quả như sau: ";
        };

        return ChatbotResponse.builder()
                .answer(answer)
                .suggestions(suggestions)
                .aiEnabled(aiEnabled)
                .intent(intentType)
                .responseType(resolveResponseType(intentType))
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
        return !isBlank(openRouterProperties.getApiKey());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String resolveResponseType(String intentType) {
        return switch (intentType) {
            case "compare" -> "product_comparison";
            case "product_info" -> "product_detail";
            case "faq" -> "text_only";
            case "unknown" -> "guide";
            default -> "product_recommendation";
        };
    }

    private String abbreviate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }

    private record ModelOutput(String answer, List<Long> productIds, String followUpQuestion, String responseType) {
    }
}
