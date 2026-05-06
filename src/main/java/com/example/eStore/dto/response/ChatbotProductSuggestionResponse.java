package com.example.eStore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotProductSuggestionResponse {
    private Long id;
    private String name;
    private Long price;
    private String categoryName;
    private String brandName;
    private String thumbnailUrl;
    private String cpu;
    private String ram;
    private Integer stockQuantity;
}
