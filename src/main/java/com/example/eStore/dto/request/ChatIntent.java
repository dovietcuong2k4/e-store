package com.example.eStore.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatIntent {
    // One of: recommend, compare, product_info, faq, unknown
    private String intent;
    private List<String> productNames;
    private String category;
    private String brand;
    private Long minPrice;
    private Long maxPrice;
    private String usage;
    private String rawMessage;
}
