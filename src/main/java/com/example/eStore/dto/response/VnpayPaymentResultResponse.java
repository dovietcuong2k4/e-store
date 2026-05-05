package com.example.eStore.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VnpayPaymentResultResponse {
    private Long orderId;
    private boolean paid;
    private String message;
    private String responseCode;
    private String transactionStatus;
    private String transactionNo;
    private String bankCode;
    private String payDate;
    private Long amount;
}
