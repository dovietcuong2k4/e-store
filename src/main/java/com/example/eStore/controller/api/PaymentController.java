package com.example.eStore.controller.api;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.response.ApiResponseFactory;
import com.example.eStore.dto.response.VnpayIpnResponse;
import com.example.eStore.dto.response.VnpayPaymentResultResponse;
import com.example.eStore.service.VnpayPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments/vnpay")
@RequiredArgsConstructor
public class PaymentController {
    private final VnpayPaymentService vnpayPaymentService;

    @GetMapping("/return")
    public ResponseEntity<BaseResultDTO<VnpayPaymentResultResponse>> vnpayReturn(
            @RequestParam Map<String, String> params) {
        VnpayPaymentResultResponse result = vnpayPaymentService.handleReturn(params);
        return ResponseEntity.ok(ApiResponseFactory.success(result.getMessage(), result));
    }

    @GetMapping("/ipn")
    public ResponseEntity<VnpayIpnResponse> vnpayIpn(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(vnpayPaymentService.handleIpn(params));
    }
}
