package com.example.eStore.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "vnpay")
public class VnpayProperties {
    private String tmnCode;
    private String hashSecret;
    private String payUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private String returnUrl = "http://localhost:4200/payment/vnpay-return";
    private String ipnUrl = "http://localhost:9090/api/payments/vnpay/ipn";
    private String version = "2.1.0";
    private String command = "pay";
    private String currencyCode = "VND";
    private String locale = "vn";
    private String orderType = "other";
}
