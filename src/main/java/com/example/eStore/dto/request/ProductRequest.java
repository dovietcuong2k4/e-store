package com.example.eStore.dto.request;

import lombok.Data;

@Data
public class ProductRequest {

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
    private Integer stockQuantity;

    private Long categoryId;
    private Long brandId;
    
    private java.util.List<ProductImageRequest> images;
}