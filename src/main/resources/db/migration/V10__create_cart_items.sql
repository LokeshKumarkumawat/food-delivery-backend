CREATE TABLE cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    cart_id BIGINT,
    menu_id BIGINT,

    quantity INT,
    price_per_unit DECIMAL(10,2),
    subtotal DECIMAL(10,2),

    CONSTRAINT fk_cartitem_cart
        FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,

    CONSTRAINT fk_cartitem_menu
        FOREIGN KEY (menu_id) REFERENCES menus(id)
);
