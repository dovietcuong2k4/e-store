package com.example.eStore.controller;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.request.PreviewVoucherRequest;
import com.example.eStore.dto.response.PreviewVoucherResponse;
import com.example.eStore.dto.response.UserVoucherResponse;
import com.example.eStore.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping("/my")
    public BaseResultDTO<List<UserVoucherResponse>> getMyVouchers() {
        return voucherService.getMyVouchers();
    }

    @PostMapping("/preview")
    public BaseResultDTO<PreviewVoucherResponse> preview(@Valid @RequestBody PreviewVoucherRequest request) {
        return voucherService.previewVoucher(request);
    }
}
