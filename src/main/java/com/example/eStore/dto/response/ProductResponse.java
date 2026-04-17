package com.example.eStore.dto.response;

import com.example.eStore.entity.Product;
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

    public static ProductResponse from(Product entity) {
        return ProductResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .price(entity.getPrice())
                .cpu(entity.getCpu())
                .ram(entity.getRam())
                .screen(entity.getScreen())
                .operatingSystem(entity.getOperatingSystem())
                .batteryCapacity(entity.getBatteryCapacity())
                .design(entity.getDesign())
                .warrantyInfo(entity.getWarrantyInfo())
                .description(entity.getDescription())
                .soldQuantity(entity.getSoldQuantity())
                .stockQuantity(entity.getStockQuantity())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : null)
                .brandId(entity.getBrand() != null ? entity.getBrand().getId() : null)
                .brandName(entity.getBrand() != null ? entity.getBrand().getName() : null)
                .images(entity.getImages() == null
                        ? List.of()
                        : entity.getImages().stream().map(ProductImageResponse::from).toList())
                .build();
    }
}
