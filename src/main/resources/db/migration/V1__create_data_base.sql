CREATE TABLE IF NOT EXISTS products (
                                     id int auto_increment primary key,
                                     product_name varchar(255) DEFAULT NULL,
                                     description varchar(255) DEFAULT NULL,
                                     tag varchar(255) DEFAULT NULL,
                                     price DECIMAL (15, 3) DEFAULT NULL,
                                     quantity int DEFAULT NULL,
                                     material varchar(255) DEFAULT NULL,
                                     dimensions varchar(255) DEFAULT NULL,
                                     weight double DEFAULT NULL,
                                     created_at datetime DEFAULT NULL,
                                     updated_at datetime DEFAULT NULL,
                                     active BOOLEAN NOT NULL DEFAULT TRUE,
                                     category_id int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS technical_specifications (
                                         id int auto_increment primary key,
                                         file_name varchar(255) DEFAULT NULL,
                                         file_url varchar(255) DEFAULT NULL,
                                         product_id int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS product_images (
                                                        id int auto_increment primary key,
                                                        url varchar(255) DEFAULT NULL,
                                                        product_id int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS categories (
                                                        id int auto_increment primary key,
                                                        category_name varchar(255) DEFAULT NULL,
                                                        description varchar(255) DEFAULT NULL,
                                                        photo_url varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS carts (
                                                        id int auto_increment primary key,
                                                        cart_token VARCHAR(255) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS cart_items (
                                                        id int auto_increment primary key,
                                                        quantity int NOT NULL CHECK (quantity > 0),
                                                        carts_id int DEFAULT NULL,
                                                        products_id int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS orders (
                                      id int auto_increment primary key,
                                      order_number VARCHAR(50) NOT NULL UNIQUE,
                                      customer_name VARCHAR(255),
                                      customer_phone VARCHAR(50),
                                      total_price DECIMAL(15, 2) NOT NULL,
                                      order_start_date datetime DEFAULT NULL,
                                      paid_status enum('PAID','NOTPAY') DEFAULT 'NOTPAY'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS order_items (
                                           id int auto_increment primary key,
                                           quantity int DEFAULT NULL,
                                           product_id int DEFAULT NULL,
                                           product_name VARCHAR(255) NOT NULL,
                                           price_at_purchase DECIMAL(15, 2) NOT NULL,
                                           tag varchar(255) DEFAULT NULL,
                                           product_active BOOLEAN NOT NULL DEFAULT TRUE,
                                           orders_id int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS users (
                                       id int auto_increment primary key,
                                       name varchar(255) DEFAULT NULL,
                                       surname varchar(255) DEFAULT NULL,
                                       lastname varchar(255) DEFAULT NULL,
                                       password varchar(255) DEFAULT NULL,
                                       phone varchar(255) DEFAULT NULL,
                                       email varchar(255) DEFAULT NULL,
                                       authorities enum('ADMIN','USER') DEFAULT 'USER'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS import_histories (
                                id INT AUTO_INCREMENT PRIMARY KEY,
                                file_name VARCHAR(255),
                                success_count INT,
                                error_count INT,
                                import_status enum('SUCCESS','PARTIAL','FAILED') DEFAULT 'PARTIAL',
                                errors_log TEXT,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS promotions (
                                              id int auto_increment primary key,
                                              url_photo varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS news (
                                          id int auto_increment primary key,
                                          name varchar(255) DEFAULT NULL,
                                          description varchar(255) DEFAULT NULL,
                                          news_photo_url varchar(255) DEFAULT NULL,
                                          create_date_news datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS companies (
                                         id int auto_increment primary key,
                                         name varchar(255) DEFAULT NULL,
                                         text varchar(255) DEFAULT NULL,
                                         logo_url varchar(255) DEFAULT NULL,
                                         email varchar(255) DEFAULT NULL,
                                         phone varchar(255) DEFAULT NULL,
                                         address varchar(255) DEFAULT NULL,
                                         requisites varchar(255) DEFAULT NULL,
                                         job_start_and_end_date varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;