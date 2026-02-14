-- Создаем вспомогательную таблицу для динамических характеристик
CREATE TABLE IF NOT EXISTS product_specifications (
                        product_id INT NOT NULL,
                        spec_name VARCHAR(255) NOT NULL,
                        spec_value VARCHAR(255) NULL,
                        PRIMARY KEY (product_id, spec_name),
                        CONSTRAINT fk_product_specs_id
                            FOREIGN KEY (product_id)
                                REFERENCES products (id)
                                ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
-- 1. Добавляем общие поля (проверь, чтобы не было дублей)
# ALTER TABLE products
#     ADD COLUMN country VARCHAR(255) NULL,
#     ADD COLUMN width INT NULL,
#     ADD COLUMN depth INT NULL,
#     ADD COLUMN height INT NULL,
#     ADD COLUMN power VARCHAR(100) NULL,
#     ADD COLUMN voltage VARCHAR(100) NULL;