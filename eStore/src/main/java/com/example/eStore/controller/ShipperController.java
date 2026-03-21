package com.example.eStore.controller;

import com.example.eStore.dto.constants.Constants;
import com.example.eStore.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shipper")
@RequiredArgsConstructor
public class ShipperController {

    private final OrderService orderService;

    @PutMapping("orders/shipping/{id}")
    public void shipping(@PathVariable Long id) {
        orderService.updateStatus(id, Constants.OrderStatus.SHIPPING);
    }

    @PutMapping("orders/complete/{id}")
    public void complete(@PathVariable Long id) {
        orderService.updateStatus(id, Constants.OrderStatus.COMPLETED);
    }
}
