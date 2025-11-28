INSERT INTO roles (name) VALUES
('ROLE_ADMIN'),
('ROLE_USER'),
('ROLE_DELIVERY');



INSERT INTO users (name, email, password, phone_number, profile_url, address, is_active, created_at, updated_at)
VALUES
('Lokesh', 'lokesh@example.com', 'password123', '9876543210', NULL, 'Jaipur, India', TRUE, NOW(), NOW()),
('Rahul Sharma', 'rahul@example.com', 'password123', '9876501234', NULL, 'Delhi, India', TRUE, NOW(), NOW()),
('Priya Singh', 'priya@example.com', 'password123', '9876505678', NULL, 'Mumbai, India', TRUE, NOW(), NOW());



-- Lokesh: ADMIN + USER
INSERT INTO users_roles (user_id, role_id) VALUES (1, 1), (1, 2);

-- Rahul: USER
INSERT INTO users_roles (user_id, role_id) VALUES (2, 2);

-- Priya: USER
INSERT INTO users_roles (user_id, role_id) VALUES (3, 2);






INSERT INTO categories (name, description) VALUES
('Pizza', 'Italian pizzas with fresh ingredients'),
('Burgers', 'Delicious burgers with premium patties'),
('Desserts', 'Cakes, ice creams and more sweets');





INSERT INTO menus (name, description, price, image_url, category_id)
VALUES
('Margherita Pizza', 'Classic cheese pizza', 299.00, NULL, 1),
('Pepperoni Pizza', 'Pepperoni with cheese', 399.00, NULL, 1),
('Veg Burger', 'A juicy veg burger', 199.00, NULL, 2),
('Chicken Burger', 'Grilled chicken burger', 249.00, NULL, 2),
('Chocolate Cake', 'Rich chocolate cake slice', 149.00, NULL, 3),
('Vanilla Ice Cream', 'Creamy vanilla scoop', 99.00, NULL, 3);



INSERT INTO orders (user_id, order_date, total_amount, order_status, payment_status)
VALUES
(1, NOW(), 648.00, 'PLACED', 'PAID'),
(2, NOW(), 448.00, 'PLACED', 'PENDING');




INSERT INTO order_items (order_id, menu_id, quantity, price_per_unit, subtotal)
VALUES
-- Order 1 items
(1, 1, 2, 299.00, 598.00),
(1, 5, 1, 149.00, 149.00),

-- Order 2 items
(2, 3, 2, 199.00, 398.00),
(2, 6, 1, 99.00, 99.00);





INSERT INTO payments (order_id, amount, payment_status, transaction_id, payment_gateway, failure_reason, payment_date, user_id)
VALUES
(1, 648.00, 'SUCCESS', 'TXN123456', 'STRIPE', NULL, NOW(), 1),
(2, 448.00, 'PENDING', NULL, 'STRIPE', 'Payment not completed', NOW(), 2);





INSERT INTO carts (user_id, promo_code)
VALUES
(1, 'NEW50'),
(2, NULL),
(3, 'WELCOME10');



INSERT INTO cart_items (cart_id, menu_id, quantity, price_per_unit, subtotal)
VALUES
(1, 2, 1, 399.00, 399.00),
(1, 6, 2, 99.00, 198.00),

(2, 4, 1, 249.00, 249.00),

(3, 5, 1, 149.00, 149.00);





INSERT INTO reviews (user_id, rating, comment, created_at, order_id, menu_id)
VALUES
(1, 9, 'Amazing taste!', NOW(), 1, 1),
(2, 7, 'Good but could be hotter.', NOW(), 2, 3),
(3, 10, 'Loved the dessert!', NOW(), 1, 5);



INSERT INTO notifications (subject, recipient, body, type, created_at, is_html)
VALUES
('Order Placed', 'lokesh@example.com', 'Your order has been placed!', 'ORDER', NOW(), FALSE),
('Payment Received', 'rahul@example.com', 'Payment processed successfully!', 'PAYMENT', NOW(), TRUE),
('Welcome', 'priya@example.com', 'Thanks for joining FoodApp!', 'GENERAL', NOW(), FALSE);
