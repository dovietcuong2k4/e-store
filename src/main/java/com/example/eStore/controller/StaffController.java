package com.example.eStore.controller;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.constants.Constants;
import com.example.eStore.dto.request.AssignShipperRequest;
import com.example.eStore.dto.response.OrderWorkflowResponse;
import com.example.eStore.dto.response.UserSummaryResponse;
import com.example.eStore.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final OrderService orderService;

    @GetMapping("/orders")
    public ResponseEntity<BaseResultDTO<List<OrderWorkflowResponse>>> getOrders() {
        return ResponseEntity.ok(orderService.getStaffOrders());
    }

    @GetMapping("/shippers")
    public ResponseEntity<BaseResultDTO<List<UserSummaryResponse>>> getShippers() {
        return ResponseEntity.ok(orderService.getAssignableShippers());
    }

    @PutMapping("/orders/{id}/process")
    public ResponseEntity<BaseResultDTO<Void>> process(@PathVariable Long id) {
        return ResponseEntity.ok(
                orderService.updateStatus(id, Constants.OrderStatus.PROCESSING, Constants.Role.STAFF));
    }

    @PutMapping("/orders/{id}/ready")
    public ResponseEntity<BaseResultDTO<Void>> ready(@PathVariable Long id) {
        return ResponseEntity.ok(
                orderService.updateStatus(id, Constants.OrderStatus.READY_FOR_SHIPPING, Constants.Role.STAFF));
    }

    @PutMapping("/orders/{id}/assign-shipper")
    public ResponseEntity<BaseResultDTO<Void>> assignShipper(@PathVariable Long id,
                                                             @Valid @RequestBody AssignShipperRequest request) {
        return ResponseEntity.ok(orderService.assignShipper(id, request));
    }

    @PutMapping("/orders/{id}/cancel")
    public ResponseEntity<BaseResultDTO<Void>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(
                orderService.updateStatus(id, Constants.OrderStatus.CANCELLED, Constants.Role.STAFF));
    }
}
