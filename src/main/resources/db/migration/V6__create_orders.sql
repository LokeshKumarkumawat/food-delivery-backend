CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    user_id BIGINT,

    order_date DATETIME,
    total_amount DECIMAL(10,2),

    order_status VARCHAR(50),
    payment_status VARCHAR(50)
);
