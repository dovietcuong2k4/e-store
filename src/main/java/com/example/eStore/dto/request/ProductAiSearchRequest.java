package com.example.eStore.dto.request;

import lombok.Data;

@Data
public class ProductAiSearchRequest {
    private String q;
    private Long categoryId;
    private Long brandId;
    private Long minPrice;
    private Long maxPrice;
    private Double minRating;
    private boolean inStockOnly;
    private int page = 0;
    private int size = 12;
}