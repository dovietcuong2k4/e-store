package com.example.eStore.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_ai_summaries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewAiSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(columnDefinition = "TEXT")
    private String pros; // Store as JSON array string or comma separated

    @Column(columnDefinition = "TEXT")
    private String cons; // Store as JSON array string or comma separated

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "external_summary", columnDefinition = "TEXT")
    private String externalSummary;

    @Column(name = "external_sources", columnDefinition = "TEXT")
    private String externalSources;

    @Column(name = "external_generated_at")
    private LocalDateTime externalGeneratedAt;

    @Column(name = "review_count")
    private Integer reviewCount;

    @Column(name = "last_generated_at")
    private LocalDateTime lastGeneratedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
