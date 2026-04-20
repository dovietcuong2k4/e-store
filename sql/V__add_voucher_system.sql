-- Create vouchers table (Template)
CREATE TABLE vouchers
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    code             VARCHAR(50) UNIQUE NOT NULL,
    discount_type    VARCHAR(20)        NOT NULL, -- PERCENTAGE, FIXED
    discount_value   DOUBLE             NOT NULL,
    min_order_value  DOUBLE             DEFAULT 0,
    start_date       DATETIME           NOT NULL,
    end_date         DATETIME           NOT NULL,
    active           BOOLEAN            DEFAULT TRUE,
    created_at       DATETIME           DEFAULT CURRENT_TIMESTAMP
);

-- Create user_vouchers table (Instance)
CREATE TABLE user_vouchers
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    voucher_id  BIGINT      NOT NULL,
    status      VARCHAR(20) NOT NULL, -- AVAILABLE, USED, EXPIRED
    assigned_at DATETIME    DEFAULT CURRENT_TIMESTAMP,
    used_at      DATETIME    NULL,

    CONSTRAINT fk_user_voucher_user
        FOREIGN KEY (user_id) REFERENCES users (id),

    CONSTRAINT fk_user_voucher_voucher
        FOREIGN KEY (voucher_id) REFERENCES vouchers (id)
);

-- Alter orders table to support discount
ALTER TABLE orders
    ADD COLUMN user_voucher_id BIGINT NULL,
    ADD COLUMN discount_amount BIGINT DEFAULT 0,
    ADD CONSTRAINT fk_order_user_voucher
        FOREIGN KEY (user_voucher_id) REFERENCES user_vouchers (id);

-- Create voucher_usage_history table (Log)
CREATE TABLE voucher_usage_history
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    voucher_id BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    order_id   BIGINT      NOT NULL,
    action     VARCHAR(50) NOT NULL, -- USED, CANCELLED
    note       VARCHAR(255) NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
