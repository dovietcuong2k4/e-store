package com.example.eStore.controller;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.request.AssignVoucherRequest;
import com.example.eStore.dto.request.CreateVoucherRequest;
import com.example.eStore.dto.response.VoucherResponse;
import com.example.eStore.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/vouchers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class AdminVoucherController {

    private final VoucherService voucherService;

    @GetMapping
    public BaseResultDTO<List<VoucherResponse>> getAllVoucherTemplates() {
        return voucherService.getAllVoucherTemplates();
    }

    @PostMapping
    public BaseResultDTO<VoucherResponse> createVoucherTemplate(@Valid @RequestBody CreateVoucherRequest request) {
        return voucherService.createVoucherTemplate(request);
    }

    @PostMapping("/assign")
    public BaseResultDTO<Void> assignVoucherToUser(@Valid @RequestBody AssignVoucherRequest request) {
        return voucherService.assignVoucherToUser(request);
    }
}
