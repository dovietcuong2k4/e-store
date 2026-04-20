CREATE TABLE product_reviews
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT   NOT NULL,
    user_id    BIGINT   NOT NULL,
    rating     INT      NOT NULL,
    comment    TEXT     NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL,

    CONSTRAINT fk_review_product
        FOREIGN KEY (product_id) REFERENCES products (id),

    CONSTRAINT fk_review_user
        FOREIGN KEY (user_id) REFERENCES users (id),

    CONSTRAINT unique_user_product UNIQUE (user_id, product_id)
);
