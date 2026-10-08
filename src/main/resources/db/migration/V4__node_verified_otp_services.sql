INSERT INTO services (name, base_url, endpoint_type, endpoint_path, http_method, request_body_template, integration, enabled)
SELECT 'Korona Pay', 'https://koronapay.com', 'OTP_REQUEST', '/transfers/online/api/users/otps', 'POST', NULL, 'KORONAPAY_OTP', TRUE
WHERE NOT EXISTS (SELECT 1 FROM services WHERE name = 'Korona Pay');

INSERT INTO services (name, base_url, endpoint_type, endpoint_path, http_method, request_body_template, integration, enabled)
SELECT 'Start.ru', 'https://start.ru', 'OTP_REQUEST', '/api/v1/auth/otp', 'POST', '{"phone":"{{phone_e164}}"}', 'START_RU_OTP', TRUE
WHERE NOT EXISTS (SELECT 1 FROM services WHERE name = 'Start.ru');

INSERT INTO services (name, base_url, endpoint_type, endpoint_path, http_method, request_body_template, integration, enabled)
SELECT 'Qlean', 'https://qlean.ru', 'OTP_REQUEST', '/clients-api/v2/sms_codes/auth/request_code', 'POST', '{"phone":"{{phone}}"}', 'QLEAN_OTP', TRUE
WHERE NOT EXISTS (SELECT 1 FROM services WHERE name = 'Qlean');

INSERT INTO services (name, base_url, endpoint_type, endpoint_path, http_method, request_body_template, integration, enabled)
SELECT 'MTS TV', 'https://prod.tvh.mts.ru', 'OTP_REQUEST', '/tvh-public-api-gateway/public/rest/general/send-code', 'POST', NULL, 'MTS_TV_OTP', TRUE
WHERE NOT EXISTS (SELECT 1 FROM services WHERE name = 'MTS TV');

INSERT INTO services (name, base_url, endpoint_type, endpoint_path, http_method, request_body_template, integration, enabled)
SELECT 'Webbankir', 'https://ng-api.webbankir.com', 'OTP_REQUEST', '/user/v2/create', 'POST', NULL, 'WEBBANKIR_OTP', TRUE
WHERE NOT EXISTS (SELECT 1 FROM services WHERE name = 'Webbankir');

UPDATE services SET enabled = FALSE WHERE name = 'MTS ID';
