package com.example.eStore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewAiSummaryResponse {
    private Long productId;
    private List<String> pros;
    private List<String> cons;
    private String summary;
    private Integer reviewCount;
    private LocalDateTime lastGeneratedAt;
    private String externalSummaryLabel;
    private String externalSummary;
    private List<ReviewAiSummarySourceResponse> externalSources;
    private LocalDateTime externalSummaryGeneratedAt;
}
