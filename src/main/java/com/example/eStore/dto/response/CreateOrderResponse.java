package com.example.eStore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponse {
    private Long id;
    private String paymentMethod;
    private String paymentStatus;
    private String paymentUrl;

    public CreateOrderResponse(Long id) {
        this.id = id;
    }
}
