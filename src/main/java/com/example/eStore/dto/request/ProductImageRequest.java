package com.example.eStore.dto.request;

import lombok.Data;

@Data
public class ProductImageRequest {
    private String imageUrl;
    private Boolean isThumbnail;
    private Integer sortOrder;
    private String publicId;
}
