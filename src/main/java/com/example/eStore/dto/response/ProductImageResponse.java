package com.example.eStore.dto.response;

import com.example.eStore.entity.ProductImage;
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

    public static ProductImageResponse from(ProductImage entity) {
        return ProductImageResponse.builder()
                .id(entity.getId())
                .imageUrl(entity.getImageUrl())
                .isThumbnail(entity.getIsThumbnail())
                .sortOrder(entity.getSortOrder())
                .publicId(entity.getPublicId())
                .build();
    }
}
