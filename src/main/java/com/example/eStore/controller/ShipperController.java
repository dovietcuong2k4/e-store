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
@RequestMapping("/api/shipper")
@RequiredArgsConstructor
public class ShipperController {

    private final OrderService orderService;

    @PutMapping("orders/shipping/{id}")
    public ResponseEntity<BaseResultDTO<Void>> shipping(@PathVariable Long id) {
        return ResponseEntity.ok(
                orderService.updateStatus(id, Constants.OrderStatus.SHIPPING, Constants.Role.SHIPPER));
    }

    @PutMapping("orders/delivered/{id}")
    public ResponseEntity<BaseResultDTO<Void>> delivered(@PathVariable Long id) {
        return ResponseEntity.ok(
                orderService.updateStatus(id, Constants.OrderStatus.DELIVERED, Constants.Role.SHIPPER));
    }

    @PutMapping("orders/delivery-failed/{id}")
    public ResponseEntity<BaseResultDTO<Void>> deliveryFailed(@PathVariable Long id) {
        return ResponseEntity.ok(
                orderService.updateStatus(id, Constants.OrderStatus.DELIVERY_FAILED, Constants.Role.SHIPPER));
    }

    @PutMapping("orders/complete/{id}")
    public ResponseEntity<BaseResultDTO<Void>> complete(@PathVariable Long id) {
        return ResponseEntity.ok(
                orderService.updateStatus(id, Constants.OrderStatus.DELIVERED, Constants.Role.SHIPPER));
    }
}
