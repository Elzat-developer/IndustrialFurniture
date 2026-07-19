-- 1. Индекс для быстрой проверки/поиска пользователя при авторизации
CREATE INDEX idx_users_email ON users(email);

-- 2. Индекс для истории заказов
-- Без него метод findAllByCustomerPhoneOrderByOrderStartDateDesc будет сканировать ВСЮ таблицу заказов
CREATE INDEX idx_orders_customer_phone ON orders(customer_phone);

-- 3. Композитный индекс для метода findSmartSimilar в ProductRepo
-- Ускоряет запрос: WHERE p.category_id = ? AND p.price BETWEEN ? AND ?
CREATE INDEX idx_products_category_price ON products(category_id, price);