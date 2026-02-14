-- Добавляем колонку типа. ENUM лучше, чем BOOLEAN, так как категорий может стать больше
ALTER TABLE products ADD COLUMN product_type ENUM('industrial', 'household') DEFAULT 'industrial' AFTER id;

-- Обязательно создаем индекс, чтобы фильтрация по типу не тормозила базу
CREATE INDEX idx_product_type ON products(product_type);