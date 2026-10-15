-- Migration V9: Remove price from products table and add price_product_status
ALTER TABLE products ADD COLUMN price_product_status VARCHAR(100) NOT NULL DEFAULT 'Уточните цену';

-- Migrate existing data
UPDATE products
SET price_product_status = CASE
    WHEN price LIKE '%наличи%' THEN 'Нет в наличии'
    WHEN price LIKE '%заказ%' THEN 'Под заказ'
    ELSE 'Уточните цену'
END
WHERE price IS NOT NULL;

-- Remove price column from products table
ALTER TABLE products DROP COLUMN price;
