package com.example.eStore.repository;

import com.example.eStore.entity.ReviewAiSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewAiSummaryRepository extends JpaRepository<ReviewAiSummary, Long> {
    Optional<ReviewAiSummary> findByProductId(Long productId);
}
