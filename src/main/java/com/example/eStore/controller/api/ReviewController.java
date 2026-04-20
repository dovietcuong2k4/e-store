package com.example.eStore.controller.api;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.request.ReviewRequest;
import com.example.eStore.dto.response.ReviewEligibilityResponse;
import com.example.eStore.dto.response.ReviewResponse;
import com.example.eStore.dto.response.ReviewSummaryResponse;
import com.example.eStore.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Get reviews for a product (public — no auth required).
     */
    @GetMapping("/{productId}")
    public ResponseEntity<BaseResultDTO<ReviewSummaryResponse>> getReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return ResponseEntity.ok(
                reviewService.getReviews(productId, PageRequest.of(page, size))
        );
    }

    /**
     * Check if current user can review this product (auth required).
     */
    @GetMapping("/{productId}/eligibility")
    public ResponseEntity<BaseResultDTO<ReviewEligibilityResponse>> checkEligibility(
            @PathVariable Long productId) {

        return ResponseEntity.ok(reviewService.checkEligibility(productId));
    }

    /**
     * Create a new review (auth required).
     */
    @PostMapping("/{productId}")
    public ResponseEntity<BaseResultDTO<ReviewResponse>> createReview(
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequest request) {

        return ResponseEntity.ok(reviewService.createReview(productId, request));
    }

    /**
     * Update an existing review (auth required, owner only).
     */
    @PutMapping("/{productId}")
    public ResponseEntity<BaseResultDTO<ReviewResponse>> updateReview(
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequest request) {

        return ResponseEntity.ok(reviewService.updateReview(productId, request));
    }

    /**
     * Delete an existing review (auth required, owner only).
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<BaseResultDTO<Void>> deleteReview(
            @PathVariable Long productId) {

        return ResponseEntity.ok(reviewService.deleteReview(productId));
    }
}
