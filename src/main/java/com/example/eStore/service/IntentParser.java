package com.example.eStore.service;

import com.example.eStore.config.OpenRouterProperties;
import com.example.eStore.dto.request.ChatIntent;
import com.example.eStore.entity.Brand;
import com.example.eStore.repository.BrandRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class IntentParser {
    private static final Pattern RANGE_PRICE_PATTERN = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*(?:-|den|toi)\\s*(\\d+(?:[.,]\\d+)?)\\s*(trieu|tr|m|k|nghin)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern PRICE_PATTERN = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*(trieu|tr|m|k|nghin)?", Pattern.CASE_INSENSITIVE);

    private static final List<String> PRODUCT_TYPE_KEYWORDS = List.of(
            "laptop", "dien thoai", "phone", "smartphone", "may tinh", "pc", "tablet",
            "tai nghe", "headphone", "ban phim", "keyboard", "chuot", "mouse", "man hinh", "monitor"
    );

    private static final Map<String, List<String>> USAGE_KEYWORDS = Map.of(
            "gaming", List.of("gaming", "game", "rtx", "gtx", "144hz", "165hz"),
            "office", List.of("van phong", "hoc tap", "sinh vien", "office", "mong nhe"),
            "design", List.of("do hoa", "thiet ke", "photoshop", "render"),
            "programming", List.of("lap trinh", "code", "dev", "developer"),
            "battery", List.of("pin", "battery", "dung lau", "pin trau")
    );

    private final OpenRouterProperties openRouterProperties;
    private final BrandRepository brandRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatIntent parseIntent(String message) {
        String normalized = normalize(message);
        String intent = detectIntent(normalized);
        PriceRange priceRange = extractPriceRange(normalized);

        List<String> productNames = new ArrayList<>();
        for (String type : PRODUCT_TYPE_KEYWORDS) {
            if (normalized.contains(type)) {
                productNames.add(type);
            }
        }

        String usage = null;
        for (Map.Entry<String, List<String>> entry : USAGE_KEYWORDS.entrySet()) {
            if (entry.getValue().stream().anyMatch(normalized::contains)) {
                usage = entry.getKey();
                break;
            }
        }

        String brand = extractBrand(normalized);

        return ChatIntent.builder()
                .intent(intent)
                .productNames(productNames)
                .category(null)
                .brand(brand)
                .minPrice(priceRange.min)
                .maxPrice(priceRange.max)
                .usage(usage)
                .rawMessage(message)
                .build();
    }

    public ChatIntent parseIntentWithAI(String message) {
        if (isBlank(message) || !hasApiKey()) {
            return parseIntent(message);
        }

        try {
            return parseIntentWithAi(message);
        } catch (Exception exception) {
            log.warn("AI intent parsing failed, using fallback parser: {}", exception.getMessage());
            return parseIntent(message);
        }
    }

    private ChatIntent parseIntentWithAi(String message) throws IOException, InterruptedException {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", openRouterProperties.getModel());
        requestBody.put("instructions", buildInstructions());
        requestBody.put("input", buildUserInput(message));
        requestBody.put("max_output_tokens", Math.min(openRouterProperties.getMaxOutputTokens(), 300));
        requestBody.put("text", Map.of("format", buildResponseFormat()));

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

        String outputText = extractOutputText(response.body());
        JsonNode node = objectMapper.readTree(outputText);

        return ChatIntent.builder()
                .intent(textValue(node, "intent", "recommend"))
                .productNames(stringList(node.path("productNames")))
                .category(nullIfBlank(textValue(node, "category", null)))
                .brand(resolveBrandName(nullIfBlank(textValue(node, "brand", null))))
                .minPrice(longValue(node.path("minPrice")))
                .maxPrice(longValue(node.path("maxPrice")))
                .usage(nullIfBlank(textValue(node, "usage", null)))
                .rawMessage(message)
                .build();
    }

    private String buildInstructions() {
        return """
                You are an intent parser for an e-commerce chatbot.
                Read the customer's message and return JSON only.
                Map the message into: intent, productNames, category, brand, minPrice, maxPrice, usage.
                intent must be one of: recommend, compare, product_info, faq, unknown.
                productNames should contain explicit product types or names mentioned by the user.
                category should be a broad store category when clear.
                brand should capture the customer brand preference when mentioned, using a known brand name if possible.
                usage should describe the user's purpose, such as gaming, office, design, programming, or battery.
                Use null for minPrice and maxPrice when the user did not specify a budget.
                Keep the response compact and do not add extra keys.
                """;
    }

    private String buildUserInput(String message) throws IOException {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("message", message);
        payload.put("knownBrands", brandRepository.findAll().stream()
                .map(Brand::getName)
                .filter(name -> name != null && !name.isBlank())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList());
        return objectMapper.writeValueAsString(payload);
    }

    private Map<String, Object> buildResponseFormat() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("additionalProperties", false);
        schema.put("required", List.of("intent", "productNames", "category", "brand", "minPrice", "maxPrice", "usage"));
        schema.put("properties", Map.of(
                "intent", Map.of(
                        "type", "string",
                        "enum", List.of("recommend", "compare", "product_info", "faq", "unknown")
                ),
                "productNames", Map.of(
                        "type", "array",
                        "items", Map.of("type", "string")
                ),
                "category", Map.of(
                        "type", List.of("string", "null")
                ),
                "brand", Map.of(
                        "type", List.of("string", "null")
                ),
                "minPrice", Map.of(
                        "type", List.of("integer", "null")
                ),
                "maxPrice", Map.of(
                        "type", List.of("integer", "null")
                ),
                "usage", Map.of(
                        "type", List.of("string", "null")
                )
        ));

        Map<String, Object> format = new LinkedHashMap<>();
        format.put("type", "json_schema");
        format.put("name", "chatbot_intent");
        format.put("strict", true);
        format.put("schema", schema);
        return format;
    }

    private List<String> stringList(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }

        List<String> values = new ArrayList<>();
        for (JsonNode item : node) {
            String value = item.asText("").trim();
            if (!value.isEmpty() && !values.contains(value)) {
                values.add(value);
            }
        }
        return values;
    }

    private Long longValue(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        if (node.canConvertToLong()) {
            return node.asLong();
        }
        String text = node.asText("").trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String textValue(JsonNode node, String fieldName, String defaultValue) {
        JsonNode field = node.path(fieldName);
        if (field.isMissingNode() || field.isNull()) {
            return defaultValue;
        }
        String value = field.asText(defaultValue);
        return value == null ? defaultValue : value.trim();
    }

    private String nullIfBlank(String value) {
        return value == null || value.isBlank() ? null : value;
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

    private boolean hasApiKey() {
        return !isBlank(openRouterProperties.getApiKey());
    }

    private String detectIntent(String normalized) {
        // Compare — most specific, check first
        if (containsAny(normalized, "so sanh", "compare", " vs ", "vs.")) return "compare";

        // FAQ — store-policy / service questions (not product-related)
        if (containsAny(normalized, "bao hanh", "doi tra", "hoan tien", "tra hang",
                "giao hang", "van chuyen", "phi ship", "phi van chuyen",
                "thanh toan", "phuong thuc thanh toan", "tra gop",
                "chinh sach", "quy dinh", "dieu khoan",
                "lien he", "hotline", "dia chi cua hang",
                "gio lam viec", "gio mo cua",
                "khieu nai", "phan anh",
                "faq", "ho tro khach hang")) return "faq";

        // Product info — asking details about a specific product
        if (containsAny(normalized, "thong tin", "chi tiet", "co gi", "cau hinh",
                "thong so", "spec", "review", "danh gia")) return "product_info";

        // Recommend — purchase advice
        if (containsAny(normalized, "goi y", "recommend", "tu van", "suggest",
                "nen mua", "mua gi", "chon gi", "giup minh chon",
                "de xuat", "phu hop")) return "recommend";

        // If message contains product-related context (type keyword or price), treat as recommend
        boolean hasProductType = PRODUCT_TYPE_KEYWORDS.stream().anyMatch(normalized::contains);
        boolean hasPrice = PRICE_PATTERN.matcher(normalized).find();
        if (hasProductType || hasPrice) return "recommend";

        // No recognisable intent
        return "unknown";
    }

    private PriceRange extractPriceRange(String normalizedMessage) {
        Matcher rangeMatcher = RANGE_PRICE_PATTERN.matcher(normalizedMessage);
        if (rangeMatcher.find()) {
            long min = toVnd(rangeMatcher.group(1), rangeMatcher.group(3));
            long max = toVnd(rangeMatcher.group(2), rangeMatcher.group(3));
            return new PriceRange(Math.min(min, max), Math.max(min, max));
        }

        Matcher matcher = PRICE_PATTERN.matcher(normalizedMessage);
        if (!matcher.find()) {
            return new PriceRange(null, null);
        }

        long value = toVnd(matcher.group(1), matcher.group(2));
        if (containsAny(normalizedMessage, "tren", "toi thieu", "min", "tu")) {
            return new PriceRange(value, null);
        }
        if (containsAny(normalizedMessage, "tam", "khoang", "around")) {
            return new PriceRange(Math.round(value * 0.8), Math.round(value * 1.2));
        }
        return new PriceRange(null, value);
    }

    private long toVnd(String rawNumber, String unit) {
        BigDecimal number = new BigDecimal(rawNumber.replace(',', '.'));
        String normalizedUnit = unit == null ? "" : unit.toLowerCase(Locale.ROOT);
        if (normalizedUnit.equals("k") || normalizedUnit.equals("nghin")) {
            return number.multiply(BigDecimal.valueOf(1_000)).longValue();
        }
        if (normalizedUnit.equals("trieu") || normalizedUnit.equals("tr") || normalizedUnit.equals("m") || number.compareTo(BigDecimal.valueOf(1000)) < 0) {
            return number.multiply(BigDecimal.valueOf(1_000_000)).longValue();
        }
        return number.longValue();
    }

    private String normalize(String value) {
        if (value == null) return "";
        String noAccent = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return noAccent.toLowerCase(Locale.ROOT);
    }

    private String extractBrand(String normalizedMessage) {
        return brandRepository.findAll().stream()
                .map(Brand::getName)
                .filter(name -> name != null && !name.isBlank())
                .sorted(Comparator.comparingInt((String name) -> normalize(name).length()).reversed())
                .filter(name -> normalizedMessage.contains(normalize(name)))
                .findFirst()
                .map(this::resolveBrandName)
                .orElse(null);
    }

    private String resolveBrandName(String candidate) {
        if (isBlank(candidate)) {
            return null;
        }

        String normalizedCandidate = normalize(candidate);
        return brandRepository.findAll().stream()
                .map(Brand::getName)
                .filter(name -> name != null && !name.isBlank())
                .filter(name -> normalize(name).equals(normalizedCandidate))
                .findFirst()
                .orElse(candidate.trim());
    }

    private boolean containsAny(String value, String... terms) {
        for (String term : terms) {
            if (value.contains(term)) return true;
        }
        return false;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static class PriceRange {
        final Long min;
        final Long max;

        PriceRange(Long min, Long max) {
            this.min = min;
            this.max = max;
        }
    }
}
