-- Modify price column in products to store price status ('Уточните цену', 'Нет в наличии', 'Под заказ')
ALTER TABLE products MODIFY COLUMN price VARCHAR(100) NOT NULL DEFAULT 'Уточните цену';

-- Update existing products to default status if not already one of the valid statuses
UPDATE products
SET price = 'Уточните цену'
WHERE price IS NULL
   OR price = ''
   OR price = '0.00'
   OR price = '0'
   OR price NOT IN ('Уточните цену', 'Нет в наличии', 'Под заказ');

-- Update order items and orders to accommodate price status strings
ALTER TABLE order_items MODIFY COLUMN price_at_purchase VARCHAR(100) DEFAULT 'Уточните цену';
ALTER TABLE orders MODIFY COLUMN total_price VARCHAR(100) DEFAULT 'Уточните цену';
