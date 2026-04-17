package com.example.eStore.controller.api;

import com.example.eStore.dto.request.BrandRequest;
import com.example.eStore.dto.response.BrandResponse;
import com.example.eStore.entity.Brand;
import com.example.eStore.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandRepository brandRepository;

    @GetMapping
    public ResponseEntity<List<BrandResponse>> getAll() {
        return ResponseEntity.ok(brandRepository.findAll().stream()
                .map(BrandResponse::from)
                .toList());
    }

    @PostMapping
    public ResponseEntity<BrandResponse> create(@RequestBody BrandRequest request) {
        Brand brand = new Brand();
        brand.setName(request.getName());
        brand.setImageUrl(request.getImageUrl());
        return ResponseEntity.ok(BrandResponse.from(brandRepository.save(brand)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BrandResponse> update(@PathVariable Long id, @RequestBody BrandRequest request) {
        return brandRepository.findById(id)
                .map(existing -> {
                    existing.setName(request.getName());
                    existing.setImageUrl(request.getImageUrl());
                    return ResponseEntity.ok(BrandResponse.from(brandRepository.save(existing)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        brandRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
