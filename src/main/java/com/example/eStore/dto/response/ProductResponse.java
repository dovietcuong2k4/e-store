package com.example.eStore.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductResponse {

    private Long id;
    private String name;
    private Long price;
    private String cpu;
    private String ram;
    private String screen;
    private String operatingSystem;
    private String batteryCapacity;
    private String design;
    private String warrantyInfo;
    private String description;
    private Integer soldQuantity;
    private Integer stockQuantity;

    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;

    private List<ProductImageResponse> images;
}
