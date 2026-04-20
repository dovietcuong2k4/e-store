package com.example.eStore.dto.response;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
@Builder
public class ReviewSummaryResponse {
    private Double averageRating;
    private Long totalReviews;
    private Page<ReviewResponse> reviews;
    private ReviewResponse currentUserReview;
}
