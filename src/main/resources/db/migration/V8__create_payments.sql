CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    order_id BIGINT,
    amount DECIMAL(10,2),

    payment_status VARCHAR(50),
    transaction_id VARCHAR(255),
    payment_gateway VARCHAR(50),
    failure_reason VARCHAR(500),
    payment_date DATETIME,

    user_id BIGINT,

    CONSTRAINT fk_payment_order
        FOREIGN KEY (order_id) REFERENCES orders(id),

    CONSTRAINT fk_payment_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);
