CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    order_id BIGINT,
    menu_id BIGINT,

    quantity INT,
    price_per_unit DECIMAL(10,2),
    subtotal DECIMAL(10,2),

    CONSTRAINT fk_orderitem_order
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,

    CONSTRAINT fk_orderitem_menu
        FOREIGN KEY (menu_id) REFERENCES menus(id)
);
