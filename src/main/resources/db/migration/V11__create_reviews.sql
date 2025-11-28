CREATE TABLE reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    user_id BIGINT NOT NULL,
    rating INT,
    comment TEXT,
    created_at DATETIME,
    order_id BIGINT,
    menu_id BIGINT,

    CONSTRAINT fk_review_user
        FOREIGN KEY (user_id) REFERENCES users(id),

    CONSTRAINT fk_review_menu
        FOREIGN KEY (menu_id) REFERENCES menus(id)
);
