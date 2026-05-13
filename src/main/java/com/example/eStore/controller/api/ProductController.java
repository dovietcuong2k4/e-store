package com.example.eStore.controller.api;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.request.ProductAiSearchRequest;
import com.example.eStore.dto.request.ProductRequest;
import com.example.eStore.dto.response.ProductResponse;
import com.example.eStore.dto.response.ProductRecommendationResponse;
import com.example.eStore.service.ProductAiSearchService;
import com.example.eStore.service.ProductService;
import com.example.eStore.service.ProductRecommendationService;
import com.example.eStore.dto.response.ReviewAiSummaryResponse;
import com.example.eStore.service.ReviewAiSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductRecommendationService productRecommendationService;
    private final ProductAiSearchService productAiSearchService;
    private final ReviewAiSummaryService reviewAiSummaryService;

    @PostMapping("/create")
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

    @GetMapping("/ai-search")
    public ResponseEntity<BaseResultDTO<Page<ProductResponse>>> aiSearch(@ModelAttribute ProductAiSearchRequest request) {
        return ResponseEntity.ok(productAiSearchService.search(request));
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<BaseResultDTO<ProductResponse>> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getDetail(id));
    }

    @PutMapping("update/{id}")
    public ResponseEntity<BaseResultDTO<ProductResponse>> update(
            @PathVariable Long id,
            @RequestBody ProductRequest request) {

        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<BaseResultDTO<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(productService.delete(id));
    }

    @GetMapping("/{id}/recommendations")
    public ResponseEntity<ProductRecommendationResponse> getRecommendations(@PathVariable Long id) {
        ProductRecommendationResponse response = productRecommendationService.getRecommendations(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/review-summary")
    public ResponseEntity<BaseResultDTO<ReviewAiSummaryResponse>> getReviewSummary(@PathVariable Long id) throws IOException, InterruptedException {
        ReviewAiSummaryResponse response = reviewAiSummaryService.getReviewSummary(id);
        return ResponseEntity.ok(BaseResultDTO.<ReviewAiSummaryResponse>builder()
                .success(true)
                .message("Success")
                .data(response)
                .build());
    }
}
