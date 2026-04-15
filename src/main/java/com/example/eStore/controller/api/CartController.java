package com.example.eStore.controller.api;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.request.AddToCartRequest;
import com.example.eStore.dto.request.UpdateCartItemRequest;
import com.example.eStore.dto.response.CartResponse;
import com.example.eStore.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<BaseResultDTO<Void>> add(@RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addToCart(request));
    }

    @GetMapping
    public ResponseEntity<BaseResultDTO<CartResponse>> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    @PutMapping("/item/{id}")
    public ResponseEntity<BaseResultDTO<Void>> update(
            @PathVariable Long id,
            @RequestBody UpdateCartItemRequest request) {

        return ResponseEntity.ok(cartService.updateCartItem(id, request.getQuantity()));
    }

    @DeleteMapping("/item/{id}")
    public ResponseEntity<BaseResultDTO<Void>> remove(@PathVariable Long id) {
        return ResponseEntity.ok(cartService.removeItem(id));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<BaseResultDTO<Void>> clear() {
        return ResponseEntity.ok(cartService.clearCart());
    }
}
