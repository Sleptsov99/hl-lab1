ALTER TABLE services
    ADD COLUMN integration VARCHAR(32) NOT NULL DEFAULT 'GENERIC_HTTP';

UPDATE services SET integration = 'YANDEX_PWL_OTP',
    base_url = 'https://passport.yandex.ru',
    endpoint_path = '/pwl-yandex/auth/add',
    http_method = 'POST',
    request_body_template = NULL
WHERE name = 'Yandex ID';

UPDATE services SET integration = 'OZON_FAST_ENTRY',
    base_url = 'https://www.ozon.ru',
    endpoint_path = '/api/composer-api.bx/_action/fastEntry',
    http_method = 'POST',
    request_body_template = '{"phone":"{{phone_e164}}","otpId":0}'
WHERE name = 'Ozon';
