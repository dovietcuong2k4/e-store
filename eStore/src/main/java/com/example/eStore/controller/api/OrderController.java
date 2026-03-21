package com.example.eStore.controller.api;

import com.example.eStore.dto.request.CreateOrderRequest;
import com.example.eStore.entity.Order;
import com.example.eStore.service.OrderService;
import lombok.RequiredArgsConstructor;
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
    public void create(@RequestBody CreateOrderRequest request) {
        orderService.createOrder(getUserId(), request);
    }

    @GetMapping
    public List<Order> getOrders() {
        return orderService.getUserOrders(getUserId());
    }
}