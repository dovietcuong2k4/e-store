package com.example.eStore.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductImageResponse {
    private Long id;
    private String imageUrl;
    private Boolean isThumbnail;
    private Integer sortOrder;
    private String publicId;
}
