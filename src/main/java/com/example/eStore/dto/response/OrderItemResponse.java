package com.example.eStore.dto.response;

import com.example.eStore.entity.OrderItem;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemResponse {
    private Long id;
    private ProductResponse product;
    private Integer quantity;
    private Long price;

    public static OrderItemResponse from(OrderItem entity) {
        return OrderItemResponse.builder()
                .id(entity.getId())
                .product(ProductResponse.from(entity.getProduct()))
                .quantity(entity.getQuantity())
                .price(entity.getPrice())
                .build();
    }
}
