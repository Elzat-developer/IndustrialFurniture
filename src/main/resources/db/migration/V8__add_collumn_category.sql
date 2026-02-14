-- Добавляем колонку типа. ENUM лучше, чем BOOLEAN, так как категорий может стать больше
ALTER TABLE categories ADD COLUMN category_type ENUM('industrial', 'household') DEFAULT 'industrial' AFTER id;

-- Обязательно создаем индекс, чтобы фильтрация по типу не тормозила базу
CREATE INDEX idx_category_type ON categories(category_type);