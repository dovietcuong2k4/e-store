package com.example.eStore.dto.response;

import com.example.eStore.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedProductDTO {
    private Long productId;
    private String productName;
    private Long price;
    private String thumbnailUrl;
    private String reason;

    public static RecommendedProductDTO from(Product product, String reason) {
        return RecommendedProductDTO.builder()
                .productId(product.getId())
                .productName(product.getName())
                .price(product.getPrice())
                .thumbnailUrl(product.getThumbnailUrl())
                .reason(reason)
                .build();
    }
}
