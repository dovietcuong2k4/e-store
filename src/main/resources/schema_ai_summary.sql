CREATE TABLE IF NOT EXISTS review_ai_summaries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    pros TEXT,
    cons TEXT,
    summary TEXT,
    external_summary TEXT,
    external_sources TEXT,
    external_generated_at DATETIME,
    review_count INT DEFAULT 0,
    last_generated_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_product_id (product_id),
    CONSTRAINT fk_review_ai_summary_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);
