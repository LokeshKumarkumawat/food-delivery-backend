CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    subject VARCHAR(255),
    recipient VARCHAR(255) NOT NULL,
    body LONGTEXT,
    type VARCHAR(50),
    created_at DATETIME,
    is_html BOOLEAN
);
