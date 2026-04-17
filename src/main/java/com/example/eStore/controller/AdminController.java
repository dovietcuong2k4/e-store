package com.example.eStore.controller;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.request.AdminUserUpsertRequest;
import com.example.eStore.dto.response.OrderWorkflowResponse;
import com.example.eStore.dto.response.UserResponse;
import com.example.eStore.service.OrderService;
import com.example.eStore.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final OrderService orderService;
    private final UserManagementService userManagementService;

    @GetMapping("/orders")
    public ResponseEntity<BaseResultDTO<List<OrderWorkflowResponse>>> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long shipperId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(orderService.getAdminOrders(status, shipperId, date));
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
