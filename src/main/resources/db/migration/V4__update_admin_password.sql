-- Обновляем пароль для существующего администратора
UPDATE `users`
SET `password` = '$2a$10$mPxl4InEvcCaoBKFcJqXDuLO1zCwk3kCwg2dbfC6sAM9PA0QVh/Iq'
WHERE `id` = 1;