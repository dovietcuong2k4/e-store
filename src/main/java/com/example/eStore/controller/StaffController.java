package com.example.eStore.controller;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.constants.Constants;
import com.example.eStore.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final OrderService orderService;

    @PutMapping("/orders/confirm/{id}")
    public ResponseEntity<BaseResultDTO<Void>> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(
                orderService.updateStatus(id, Constants.OrderStatus.CONFIRMED, Constants.Role.STAFF));
    }

    @PutMapping("/orders/prepare/{id}")
    public ResponseEntity<BaseResultDTO<Void>> prepare(@PathVariable Long id) {
        return ResponseEntity.ok(
                orderService.updateStatus(id, Constants.OrderStatus.PREPARING, Constants.Role.STAFF));
    }

    @PutMapping("/orders/ready/{id}")
    public ResponseEntity<BaseResultDTO<Void>> ready(@PathVariable Long id) {
        return ResponseEntity.ok(
                orderService.updateStatus(id, Constants.OrderStatus.READY_FOR_SHIPPING, Constants.Role.STAFF));
    }
}
