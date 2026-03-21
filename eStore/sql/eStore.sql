CREATE DATABASE IF NOT EXISTS online_store
DEFAULT CHARACTER SET utf8;

USE online_store;

SET FOREIGN_KEY_CHECKS = 0;

-- =============================
-- DROP TABLES (child -> parent)
-- =============================
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS carts;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS contacts;
DROP TABLE IF EXISTS brands;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================
-- USERS
-- =============================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255),
    email VARCHAR(255),
    password VARCHAR(255),
    phone VARCHAR(50),
    address VARCHAR(255)
);

-- =============================
-- ROLES
-- =============================
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255)
);

-- =============================
-- USER_ROLES
-- =============================
CREATE TABLE user_roles (
    user_id BIGINT,
    role_id BIGINT,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- =============================
-- CATEGORIES
-- =============================
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255)
);

-- =============================
-- BRANDS
-- =============================
CREATE TABLE brands (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255)
);

-- =============================
-- PRODUCTS
-- =============================
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    price BIGINT NOT NULL,
    cpu VARCHAR(255),
    ram VARCHAR(255),
    screen VARCHAR(255),
    operating_system VARCHAR(255),
    battery_capacity VARCHAR(255),
    design VARCHAR(255),
    warranty_info VARCHAR(255),
    description VARCHAR(255),
    sold_quantity INT,
    stock_quantity INT,
    category_id BIGINT,
    brand_id BIGINT,
    CONSTRAINT fk_product_category
        FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_product_brand
        FOREIGN KEY (brand_id) REFERENCES brands(id)
);

-- =============================
-- CARTS
-- =============================
CREATE TABLE carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    total_price BIGINT,
    CONSTRAINT fk_cart_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);

-- =============================
-- CART ITEMS
-- =============================
CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT,
    product_id BIGINT,
    quantity INT NOT NULL,
    CONSTRAINT fk_cart_item_cart
        FOREIGN KEY (cart_id) REFERENCES carts(id),
    CONSTRAINT fk_cart_item_product
        FOREIGN KEY (product_id) REFERENCES products(id)
);

-- =============================
-- ORDERS
-- =============================
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    shipper_id BIGINT,
    receiver_name VARCHAR(255),
    receiver_phone VARCHAR(50),
    receiver_address VARCHAR(255),
    note VARCHAR(255),
    order_date DATETIME,
    shipping_date DATETIME,
    received_date DATETIME,
    status VARCHAR(50),
    CONSTRAINT fk_order_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_order_shipper
        FOREIGN KEY (shipper_id) REFERENCES users(id)
);

-- =============================
-- ORDER ITEMS
-- =============================
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT,
    product_id BIGINT,
    quantity INT,
    price BIGINT,
    CONSTRAINT fk_order_item_order
        FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_item_product
        FOREIGN KEY (product_id) REFERENCES products(id)
);

-- =============================
-- CONTACTS
-- =============================
CREATE TABLE contacts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255),
    message VARCHAR(255),
    reply_message VARCHAR(255),
    contact_date DATETIME,
    reply_date DATETIME,
    status VARCHAR(50),
    responder_id BIGINT,
    CONSTRAINT fk_contact_responder
        FOREIGN KEY (responder_id) REFERENCES users(id)
);


-- =============================
-- ROLES
-- =============================
INSERT INTO roles (id, name) VALUES
(1,'ROLE_ADMIN'),
(2,'ROLE_CUSTOMER'),
(3,'ROLE_SHIPPER'),
(4,'ROLE_STAFF');

-- =============================
-- USERS
-- =============================
INSERT INTO users (id,address,email,full_name,password,phone) VALUES
(1,NULL,'admin@gmail.com','Đỗ Việt Cường','$2a$10$/VFMNUPBKNVRMjxPFCYKZ.lKahoLQda0EaAxdqoun1w3DqwNLa2me','123456789'),
(2,NULL,'member@gmail.com','Đỗ Việt Cường','$2a$10$j7Upgupou72GBmukz0G6pOATk3wlCAgaoFCEqAhSvLToD/V/1wlpu',NULL),
(3,NULL,'shipper@gmail.com','Đỗ Việt Cường','$2a$10$u2B29HDxuWVYY3fUJ8R2qunNzXngfxij5GpvlFAEtIz3JpK/WFXF2',NULL),
(4,'Ha Noi','nguoidung@gmail.com','Vi Hải Anh','$2a$10$ZCqCO9gSWt8A8HNXAWq8luqfNbJm0uG3PsUlzry0aRLwO3VHQZTmy','123456'),
(5,NULL,'staff@gmail.com','Đỗ Việt Cường','$2a$10$u2B29HDxuWVYY3fUJ8R2qunNzXngfxij5GpvlFAEtIz3JpK/WFXF2',NULL);

-- =============================
-- USER ROLES
-- =============================
INSERT INTO user_roles (user_id,role_id) VALUES
(1,1),
(2,2),
(4,2),
(3,3),
(5,4);

-- =============================
-- CATEGORIES
-- =============================
INSERT INTO categories (id,name) VALUES
(1,'Laptop'),
(2,'PC & Gaming PC'),
(3,'Audio & Entertainment Devices'),
(4,'Computer Components'),
(5,'Storage Devices'),
(6,'Networking Devices'),
(7,'Security Cameras'),
(8,'Accessories'),
(9,'Office Equipment');

-- =============================
-- BRANDS
-- =============================
INSERT INTO brands (id,name) VALUES
(2,'Apple'),
(3,'Asus'),
(4,'Acer'),
(5,'Dell'),
(6,'HP'),
(7,'Lenovo'),
(8,'MSI'),
(9,'Masstel'),
(10,'Haier');

-- =============================
-- PRODUCTS
-- =============================
INSERT INTO products
(id,cpu,price,sold_quantity,stock_quantity,battery_capacity,operating_system,screen,ram,name,design,warranty_info,description,category_id,brand_id)
VALUES
(3,'Intel Core i5 1.8 Ghz',23990000,0,100,'5800mAh','Mac OS','13.3 inch LED','8 GB','Macbook Air 13 128GB (2017)',
'Aluminum ultra thin','12 months','Office laptop',1,2),

(4,'Intel Core i5 1.8 Ghz',28990000,0,100,'6000mAh','Mac OS','13.3 inch LED','8 GB',
'Macbook Air 13 256GB (2017)',
'Aluminum design','12 months','Good battery life',1,2),

(5,'Intel Core M3 1.2 GHz',33990000,0,150,'6000mAh','Mac OS','12 inch LED','8 GB',
'Macbook 12 256GB (2017)',
'Premium slim','12 months','Elegant laptop',1,2),

(6,'Intel Core i5 2.3 GHz',33990000,0,200,'6000mAh','Mac OS','13.3 inch LED','8 GB',
'Macbook Pro 13 128GB (2017)',
'Aluminum slim','12 months','Performance laptop',1,2),

(7,'Intel Core i5 2.3GHz',44990000,0,100,'7000mAh','Mac OS','13.3 Retina','8 GB',
'Macbook Pro 13 Touch Bar (2018)',
'Aluminum premium','12 months','High performance',1,2);

-- =============================
-- CARTS
-- =============================
INSERT INTO carts (id,user_id,total_price) VALUES
(1,2,NULL),
(2,1,NULL);

-- =============================
-- CART ITEMS
-- =============================
INSERT INTO cart_items (id,quantity,cart_id,product_id) VALUES
(10,2,1,3),
(11,1,1,4),
(12,1,1,5),
(13,1,2,6),
(14,2,2,7);

-- =============================
-- ORDERS
-- =============================
INSERT INTO orders
(id,receiver_address,note,receiver_name,order_date,shipping_date,received_date,receiver_phone,status,user_id,shipper_id)
VALUES
(31,'Ha Noi','Deliver quickly','Nguyen Van A','2024-01-10 10:00:00',NULL,NULL,'0900000001','PENDING',2,NULL),

(32,'Ha Noi','Call before delivery','Tran Van B','2024-01-11 14:00:00',NULL,NULL,'0900000002','CREATED',2,3);

-- =============================
-- ORDER ITEMS
-- =============================
INSERT INTO order_items (id,price,quantity,order_id,product_id) VALUES
(1,23990000,1,31,3),
(2,28990000,1,31,4),
(3,33990000,1,32,5);

-- =============================
-- CONTACTS
-- =============================
INSERT INTO contacts
(id,email,message,reply_message,contact_date,reply_date,status,responder_id)
VALUES
(1,'customer@gmail.com','I need support',NULL,'2024-01-10 09:00:00',NULL,'NEW',NULL);