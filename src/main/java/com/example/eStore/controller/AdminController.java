package com.example.eStore.controller;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.constants.Constants;
import com.example.eStore.dto.request.AdminUserUpsertRequest;
import com.example.eStore.dto.response.UserResponse;
import com.example.eStore.service.OrderService;
import com.example.eStore.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final OrderService orderService;
    private final UserManagementService userManagementService;

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

    @GetMapping("/users")
    public ResponseEntity<BaseResultDTO<List<UserResponse>>> getUsers() {
        return ResponseEntity.ok(userManagementService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<BaseResultDTO<UserResponse>> getUserDetail(@PathVariable Long id) {
        return ResponseEntity.ok(userManagementService.getUserById(id));
    }

    @PostMapping("/users")
    public ResponseEntity<BaseResultDTO<UserResponse>> createUser(@RequestBody AdminUserUpsertRequest request) {
        return ResponseEntity.ok(userManagementService.createUser(request));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<BaseResultDTO<UserResponse>> updateUser(@PathVariable Long id,
                                                                  @RequestBody AdminUserUpsertRequest request) {
        return ResponseEntity.ok(userManagementService.updateUser(id, request));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<BaseResultDTO<Void>> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userManagementService.deleteUser(id));
    }
}
