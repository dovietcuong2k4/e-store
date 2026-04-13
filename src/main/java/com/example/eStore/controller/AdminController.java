package com.example.eStore.controller;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.constants.Constants;
import com.example.eStore.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final OrderService orderService;

    @PutMapping("/orders/confirm/{id}")
    public ResponseEntity<BaseResultDTO<Void>> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(
                orderService.updateStatus(id, Constants.OrderStatus.CONFIRMED, Constants.Role.ADMIN));
    }

    @PutMapping("/orders/cancel/{id}")
    public ResponseEntity<BaseResultDTO<Void>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(
                orderService.updateStatus(id, Constants.OrderStatus.CANCELLED, Constants.Role.ADMIN));
    }
}
