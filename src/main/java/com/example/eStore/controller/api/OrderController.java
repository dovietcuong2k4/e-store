package com.example.eStore.controller.api;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.request.CreateOrderRequest;
import com.example.eStore.entity.Order;
import com.example.eStore.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    private Long getUserId() {
        return 2L;
    }

    @PostMapping
    public ResponseEntity<BaseResultDTO<Void>> create(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(getUserId(), request));
    }

    @GetMapping
    public ResponseEntity<BaseResultDTO<List<Order>>> getOrders() {
        return ResponseEntity.ok(orderService.getUserOrders(getUserId()));
    }
}
