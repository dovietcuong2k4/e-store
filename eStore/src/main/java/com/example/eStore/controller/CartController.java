package com.example.eStore.controller;

import com.example.eStore.dto.request.AddToCartRequest;
import com.example.eStore.dto.request.UpdateCartItemRequest;
import com.example.eStore.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // giả lập userId = 2
    private Long getUserId() {
        return 2L;
    }

    @PostMapping("/add")
    public void add(@RequestBody AddToCartRequest request) {
        cartService.addToCart(getUserId(), request);
    }

    @PutMapping("/item/{id}")
    public void update(
            @PathVariable Long id,
            @RequestBody UpdateCartItemRequest request) {

        cartService.updateCartItem(id, request.getQuantity());
    }

    @DeleteMapping("/item/{id}")
    public void remove(@PathVariable Long id) {
        cartService.removeItem(id);
    }
}