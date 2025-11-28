CREATE TABLE menus (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255),
    description TEXT,
    price DECIMAL(10,2),
    image_url VARCHAR(500),

    category_id BIGINT,

    CONSTRAINT fk_menu_category
        FOREIGN KEY (category_id) REFERENCES categories(id)
);
