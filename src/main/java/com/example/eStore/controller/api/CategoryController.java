package com.example.eStore.controller.api;

import com.example.eStore.dto.request.CategoryRequest;
import com.example.eStore.dto.response.CategoryResponse;
import com.example.eStore.entity.Category;
import com.example.eStore.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll() {
        return ResponseEntity.ok(categoryRepository.findAll().stream()
                .map(CategoryResponse::from)
                .toList());
    }

    @PostMapping("/create")
    public ResponseEntity<CategoryResponse> create(@RequestBody CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        return ResponseEntity.ok(CategoryResponse.from(categoryRepository.save(category)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable Long id, @RequestBody CategoryRequest request) {
        return categoryRepository.findById(id)
                .map(existing -> {
                    existing.setName(request.getName());
                    return ResponseEntity.ok(CategoryResponse.from(categoryRepository.save(existing)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
