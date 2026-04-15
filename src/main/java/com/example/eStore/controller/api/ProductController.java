package com.example.eStore.controller.api;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.request.ProductRequest;
import com.example.eStore.dto.response.ProductResponse;
import com.example.eStore.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<BaseResultDTO<ProductResponse>> create(@RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.create(request));
    }

    @GetMapping
    public ResponseEntity<BaseResultDTO<Page<ProductResponse>>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(productService.getAll(
                keyword,
                PageRequest.of(page, size)));
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<BaseResultDTO<ProductResponse>> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getDetail(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResultDTO<ProductResponse>> update(
            @PathVariable Long id,
            @RequestBody ProductRequest request) {

        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResultDTO<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(productService.delete(id));
    }
}
