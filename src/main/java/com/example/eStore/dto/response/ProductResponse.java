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

    private String categoryName;
    private String brandName;

    private List<ProductImageResponse> images;
}
