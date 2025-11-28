CREATE TABLE carts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    user_id BIGINT UNIQUE,
    promo_code VARCHAR(255),

    CONSTRAINT fk_cart_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);

