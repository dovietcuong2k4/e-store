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

    private Long categoryId;
    private Long brandId;
}