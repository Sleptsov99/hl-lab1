# Spamer

Внутренний монолитный инструмент для стресс-тестирования auth API (смена пароля, OTP и т.д.).

## Архитектура (модули в одном Spring Boot приложении)

- `auth` — аутентификация (JWT)
- `spam` — управление кампаниями
- `gateway` — REST API
- `queue` — очередь и отправка запросов
- `payment` — лимиты free/pro
- `proxy` — провайдер прокси
- `outer` — клиент внешних API
- `ban` — ban operator
- `admin` — администрирование
- `domain` + `repository` — users, services, proxy, spam_log

## Требования

- Docker и Docker Compose (Java на хост **не нужна**)

## Локальный запуск

```bash
docker compose up --build
```

UI: http://localhost:8080  
Логин по умолчанию: `admin` / `admin`

## Production

Домен: https://roma-huesos.duckdns.org:8443

На сервере:

```bash
mkdir -p /opt/spamer/deploy
cp deploy/.env.example /opt/spamer/deploy/.env
# отредактируйте пароли в .env
```

Деплой выполняется автоматически при push в `master` (GitHub Actions).

### GitHub Secrets

| Secret | Значение |
|--------|----------|
| `DEPLOY_HOST` | `212.113.109.203` |
| `DEPLOY_SSH_KEY` | приватный SSH ключ |

## CI

На push/PR в `develop` и `master`:

- checkstyle (линтер)
- unit/integration tests
- сборка Docker-образа
