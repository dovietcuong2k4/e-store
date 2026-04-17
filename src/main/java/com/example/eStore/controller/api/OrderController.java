package com.example.eStore.controller.api;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.request.CreateOrderRequest;
import com.example.eStore.dto.response.CreateOrderResponse;
import com.example.eStore.dto.response.OrderWorkflowResponse;
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

    @PostMapping
    public ResponseEntity<BaseResultDTO<CreateOrderResponse>> create(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }


    @GetMapping
    public ResponseEntity<BaseResultDTO<List<OrderWorkflowResponse>>> getOrders() {
        return ResponseEntity.ok(orderService.getUserOrders());
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BaseResultDTO<Void>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(
                orderService.updateStatus(id, com.example.eStore.dto.constants.Constants.OrderStatus.CANCELLED, com.example.eStore.dto.constants.Constants.Role.CUSTOMER));
    }
}
