-- 1. Категории
CREATE TABLE IF NOT EXISTS categories (
                                          id INT AUTO_INCREMENT PRIMARY KEY,
                                          category_type ENUM('industrial', 'household') DEFAULT 'industrial',
                                          category_name VARCHAR(255) DEFAULT NULL,
                                          description VARCHAR(255) DEFAULT NULL,
                                          photo_url VARCHAR(255) DEFAULT NULL,
                                          active BOOLEAN NOT NULL DEFAULT TRUE, -- Тот самый флаг для архива
                                          INDEX idx_category_type (category_type),
                                          INDEX idx_category_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 2. Продукты
CREATE TABLE IF NOT EXISTS products (
                                        id INT AUTO_INCREMENT PRIMARY KEY,
                                        product_type ENUM('industrial', 'household') DEFAULT 'industrial',
                                        product_name VARCHAR(255) DEFAULT NULL,
                                        description VARCHAR(255) DEFAULT NULL,
                                        tag VARCHAR(255) DEFAULT NULL,
                                        price DECIMAL(15, 2) DEFAULT NULL,
                                        quantity INT DEFAULT NULL,
                                        material VARCHAR(255) DEFAULT NULL,
                                        dimensions VARCHAR(255) DEFAULT NULL,
                                        weight DOUBLE DEFAULT NULL,
                                        created_at DATETIME DEFAULT NULL,
                                        updated_at DATETIME DEFAULT NULL,
                                        active BOOLEAN NOT NULL DEFAULT TRUE, -- Флаг архивации товара
                                        category_id INT DEFAULT NULL,
                                        INDEX idx_product_type (product_type),
                                        INDEX idx_product_active (active),
    -- Убираем CASCADE, ставим RESTRICT или SET NULL, чтобы нельзя было случайно удалить категорию, в которой есть товары
                                        CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3. Технические спецификации (Файлы остаются в базе, даже если товар в архиве)
CREATE TABLE IF NOT EXISTS technical_specifications (
                                                        id INT AUTO_INCREMENT PRIMARY KEY,
                                                        file_name VARCHAR(255) DEFAULT NULL,
                                                        file_url VARCHAR(255) DEFAULT NULL,
                                                        product_id INT DEFAULT NULL,
                                                        CONSTRAINT fk_tech_spec_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 4. Изображения продуктов (Картинки не удаляются)
CREATE TABLE IF NOT EXISTS product_images (
                                              id INT AUTO_INCREMENT PRIMARY KEY,
                                              url VARCHAR(255) DEFAULT NULL,
                                              product_id INT DEFAULT NULL,
                                              CONSTRAINT fk_images_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 5. Динамические характеристики
CREATE TABLE IF NOT EXISTS product_specifications (
                                                      product_id INT NOT NULL,
                                                      spec_name VARCHAR(255) NOT NULL,
                                                      spec_value VARCHAR(255) NULL,
                                                      PRIMARY KEY (product_id, spec_name),
                                                      CONSTRAINT fk_product_specs_id FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Остальные таблицы (Заказы, Пользователи и т.д.) остаются без изменений,
-- так как они должны хранить историю вечно.

-- 6. Корзины
CREATE TABLE IF NOT EXISTS carts (
                                     id INT AUTO_INCREMENT PRIMARY KEY,
                                     cart_token VARCHAR(255) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS cart_items (
                                          id INT AUTO_INCREMENT PRIMARY KEY,
                                          quantity INT NOT NULL CHECK (quantity > 0),
                                          carts_id INT DEFAULT NULL,
                                          products_id INT DEFAULT NULL,
                                          CONSTRAINT fk_cart_items_cart FOREIGN KEY (carts_id) REFERENCES carts (id) ON DELETE CASCADE,
                                          CONSTRAINT fk_cart_items_product FOREIGN KEY (products_id) REFERENCES products (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 7. Заказы (История покупок)
CREATE TABLE IF NOT EXISTS orders (
                                      id INT AUTO_INCREMENT PRIMARY KEY,
                                      order_number VARCHAR(50) NOT NULL UNIQUE,
                                      customer_name VARCHAR(255),
                                      customer_phone VARCHAR(50),
                                      total_price DECIMAL(15, 2) NOT NULL,
                                      order_start_date DATETIME DEFAULT NULL,
                                      paid_status ENUM('PAID','NOTPAY') DEFAULT 'NOTPAY'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS order_items (
                                           id INT AUTO_INCREMENT PRIMARY KEY,
                                           quantity INT DEFAULT NULL,
                                           product_id INT DEFAULT NULL,
                                           product_name VARCHAR(255) NOT NULL,
                                           price_at_purchase DECIMAL(15, 2) NOT NULL,
                                           tag VARCHAR(255) DEFAULT NULL,
                                           product_active BOOLEAN NOT NULL DEFAULT TRUE,
                                           orders_id INT DEFAULT NULL,
                                           CONSTRAINT fk_order_items_order FOREIGN KEY (orders_id) REFERENCES orders (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 8. Пользователи
CREATE TABLE IF NOT EXISTS users (
                                     id INT AUTO_INCREMENT PRIMARY KEY,
                                     name VARCHAR(255) DEFAULT NULL,
                                     surname VARCHAR(255) DEFAULT NULL,
                                     lastname VARCHAR(255) DEFAULT NULL,
                                     password VARCHAR(255) DEFAULT NULL,
                                     phone VARCHAR(255) DEFAULT NULL,
                                     email VARCHAR(255) DEFAULT NULL,
                                     authorities ENUM('ADMIN','USER') DEFAULT 'USER'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 9. История импорта
CREATE TABLE IF NOT EXISTS import_histories (
                                                id INT AUTO_INCREMENT PRIMARY KEY,
                                                file_name VARCHAR(255),
                                                success_count INT,
                                                error_count INT,
                                                import_status ENUM('SUCCESS','PARTIAL','FAILED') DEFAULT 'PARTIAL',
                                                errors_log TEXT,
                                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 10. Акции и Новости
CREATE TABLE IF NOT EXISTS promotions (
                                          id INT AUTO_INCREMENT PRIMARY KEY,
                                          url_photo VARCHAR(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS news (
                                    id INT AUTO_INCREMENT PRIMARY KEY,
                                    name VARCHAR(255) DEFAULT NULL,
                                    description TEXT,
                                    news_photo_url VARCHAR(255) DEFAULT NULL,
                                    create_date_news DATETIME DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 11. Компания
CREATE TABLE IF NOT EXISTS companies (
                                         id INT AUTO_INCREMENT PRIMARY KEY,
                                         name VARCHAR(255) DEFAULT NULL,
                                         text TEXT,
                                         logo_url VARCHAR(255) DEFAULT NULL,
                                         email VARCHAR(255) DEFAULT NULL,
                                         phone VARCHAR(255) DEFAULT NULL,
                                         address VARCHAR(255) DEFAULT NULL,
                                         requisites VARCHAR(255) DEFAULT NULL,
                                         job_start_and_end_date VARCHAR(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;