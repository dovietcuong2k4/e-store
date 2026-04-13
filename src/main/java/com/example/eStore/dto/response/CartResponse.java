package com.example.eStore.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CartResponse {
    private Long id;
    private Long userId;
    private Long totalPrice;
    private List<CartItemResponse> items;
}
