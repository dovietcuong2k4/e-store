package com.example.eStore.dto.request;

import lombok.Data;

@Data
public class CreateOrderRequest {

    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String note;
}