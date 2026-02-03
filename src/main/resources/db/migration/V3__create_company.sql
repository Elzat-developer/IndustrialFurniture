INSERT INTO companies (id, name, text, email, phone, address,requisites,job_start_and_end_date)
VALUES (
           1,
           'Industrial Furniture',
           'Краткое описание вашей компании. Админ заменит этот текст после первого входа.',
           'if@bk.ru', -- Используйте заглушку или реальный путь
           '+777777777',
           'Алматы',
            'AO Industrial Furniture',
            '09:00 18:00'
       )
ON DUPLICATE KEY UPDATE
    text = VALUES(text);