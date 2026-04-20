package com.example.eStore.service;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.constants.Constants;
import com.example.eStore.dto.request.ReviewRequest;
import com.example.eStore.dto.response.*;
import com.example.eStore.entity.Product;
import com.example.eStore.entity.ProductReview;
import com.example.eStore.entity.User;
import com.example.eStore.exception.AppException;
import com.example.eStore.repository.OrderRepository;
import com.example.eStore.repository.ProductRepository;
import com.example.eStore.repository.ProductReviewRepository;
import com.example.eStore.repository.UserRepository;
import com.example.eStore.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    /**
     * Get reviews for a product with summary statistics.
     * Publicly accessible — currentUserReview is populated only if the caller is authenticated.
     */
    public BaseResultDTO<ReviewSummaryResponse> getReviews(Long productId, Pageable pageable) {
        verifyProductExists(productId);

        Double avgRating = reviewRepository.getAverageRatingByProductId(productId);
        long totalReviews = reviewRepository.countByProductId(productId);
        Page<ReviewResponse> reviews = reviewRepository
                .findByProductIdOrderByCreatedAtDesc(productId, pageable)
                .map(ReviewResponse::from);

        // Include current user's review if authenticated
        ReviewResponse currentUserReview = null;
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId != null) {
            currentUserReview = reviewRepository.findByProductIdAndUserId(productId, userId)
                    .map(ReviewResponse::from)
                    .orElse(null);
        }

        return ApiResponseFactory.success(
                Constants.Message.Review.GET_SUCCESS,
                ReviewSummaryResponse.builder()
                        .averageRating(avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0.0)
                        .totalReviews(totalReviews)
                        .reviews(reviews)
                        .currentUserReview(currentUserReview)
                        .build()
        );
    }

    /**
     * Check if the current user is eligible to review a product.
     */
    public BaseResultDTO<ReviewEligibilityResponse> checkEligibility(Long productId) {
        Long userId = SecurityUtils.getCurrentUserId();
        verifyProductExists(productId);

        // Already reviewed?
        if (reviewRepository.existsByProductIdAndUserId(productId, userId)) {
            return ApiResponseFactory.success(
                    Constants.Message.Review.GET_SUCCESS,
                    ReviewEligibilityResponse.builder()
                            .canReview(false)
                            .reason("Bạn đã đánh giá sản phẩm này rồi")
                            .build()
            );
        }

        // Has purchased with DELIVERED status?
        if (!hasPurchasedProduct(userId, productId)) {
            return ApiResponseFactory.success(
                    Constants.Message.Review.GET_SUCCESS,
                    ReviewEligibilityResponse.builder()
                            .canReview(false)
                            .reason("Bạn cần mua và nhận sản phẩm này trước khi đánh giá")
                            .build()
            );
        }

        return ApiResponseFactory.success(
                Constants.Message.Review.GET_SUCCESS,
                ReviewEligibilityResponse.builder()
                        .canReview(true)
                        .build()
        );
    }

    /**
     * Create a new review for a product.
     */
    @Transactional
    public BaseResultDTO<ReviewResponse> createReview(Long productId, ReviewRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Product product = findProductOrThrow(productId);
        User user = findUserOrThrow(userId);

        // Business rule: must have purchased the product (DELIVERED order)
        if (!hasPurchasedProduct(userId, productId)) {
            throw new AppException(
                    "You must purchase this product before reviewing",
                    Constants.ErrorCode.Review.NOT_PURCHASED
            );
        }

        // Business rule: only one review per user per product
        if (reviewRepository.existsByProductIdAndUserId(productId, userId)) {
            throw new AppException(
                    "You have already reviewed this product",
                    Constants.ErrorCode.Review.ALREADY_REVIEWED
            );
        }

        ProductReview review = ProductReview.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        reviewRepository.save(review);

        return ApiResponseFactory.success(
                Constants.Message.Review.CREATE_SUCCESS,
                ReviewResponse.from(review)
        );
    }

    /**
     * Update an existing review (only the owner can edit).
     */
    @Transactional
    public BaseResultDTO<ReviewResponse> updateReview(Long productId, ReviewRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        ProductReview review = findUserReviewOrThrow(productId, userId);

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        reviewRepository.save(review);

        return ApiResponseFactory.success(
                Constants.Message.Review.UPDATE_SUCCESS,
                ReviewResponse.from(review)
        );
    }

    /**
     * Delete an existing review (only the owner can delete).
     */
    @Transactional
    public BaseResultDTO<Void> deleteReview(Long productId) {
        Long userId = SecurityUtils.getCurrentUserId();
        ProductReview review = findUserReviewOrThrow(productId, userId);

        reviewRepository.delete(review);

        return ApiResponseFactory.success(Constants.Message.Review.DELETE_SUCCESS);
    }

    // ─── Private Helpers ───────────────────────────────────────────

    private void verifyProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new AppException(
                    "Product not found",
                    Constants.ErrorCode.Review.PRODUCT_NOT_FOUND
            );
        }
    }

    private Product findProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new AppException(
                        "Product not found",
                        Constants.ErrorCode.Review.PRODUCT_NOT_FOUND
                ));
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(
                        "User not found",
                        Constants.ErrorCode.User.ADMIN_GET_NOT_FOUND
                ));
    }

    private ProductReview findUserReviewOrThrow(Long productId, Long userId) {
        ProductReview review = reviewRepository.findByProductIdAndUserId(productId, userId)
                .orElseThrow(() -> new AppException(
                        "Review not found",
                        Constants.ErrorCode.Review.NOT_FOUND
                ));

        if (!review.getUser().getId().equals(userId)) {
            throw new AppException(
                    "You can only modify your own review",
                    Constants.ErrorCode.Review.NOT_OWNER
            );
        }

        return review;
    }

    /**
     * Check if user has at least one DELIVERED order containing the given product.
     */
    private boolean hasPurchasedProduct(Long userId, Long productId) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId)
                .stream()
                .filter(order -> Constants.OrderStatus.DELIVERED.equals(order.getStatus()))
                .flatMap(order -> order.getOrderItems().stream())
                .anyMatch(item -> item.getProduct().getId().equals(productId));
    }
}
