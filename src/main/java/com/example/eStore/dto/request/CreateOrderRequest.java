package com.example.eStore.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String note;
    private List<CartItemRequest> items;
}