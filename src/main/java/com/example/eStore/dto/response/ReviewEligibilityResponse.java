package com.example.eStore.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewEligibilityResponse {
    private boolean canReview;
    private String reason;
}
