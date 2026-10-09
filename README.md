# Spamer Monolith — Лабораторная работа №1

Монолит на Spring Boot для учебной предметной области «Spamer» (сервис условной
рассылки с биллингом и подписками). Это **учебный проект**: CRUD + биллинг +
реальная HTTP-отправка на **свой тестовый приёмник**.

`POST /spam/send` действительно делает HTTP-запросы, но строго на **один** адрес
из настройки `spamer.sender.target-url` (env `SENDER_TARGET_URL`). Адрес берётся
только из конфига — тело запроса не может выбрать, куда идёт соединение, поэтому
приложение способно обращаться лишь к тому приёмнику, который задал оператор:
встроенному `/api/v1/test-receiver/inbox` или вашему тестовому серверу. Прокси —
связь в БД (требование лабы по M2M), трафик через них не маршрутизируется.
«Провайдер прокси» — локальная заглушка, возвращающая прокси из своей таблицы.

## Стек

Java 17 · Spring Boot 3.5 · Spring Web MVC · Spring Data JPA (Hibernate) ·
PostgreSQL 16 · Liquibase · Bean Validation · MapStruct · Springdoc OpenAPI ·
JUnit 5 + Testcontainers + Mockito · JaCoCo (≥70%) · Docker.

## Запуск

### Через Docker Compose (БД + приложение)

```bash
docker compose up --build
```

Приложение поднимется на `http://localhost:8080`, Postgres — на `5432`.

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Health: http://localhost:8080/actuator/health

### Локально (нужен запущенный Postgres)

```bash
export DB_URL=jdbc:postgresql://localhost:5432/spamer
export DB_USERNAME=spamer
export DB_PASSWORD=spamer
mvn spring-boot:run
```

Схему БД создаёт Liquibase на старте; Hibernate работает в режиме
`ddl-auto=validate` и только проверяет соответствие.

## Тесты и покрытие

```bash
mvn verify
```

- Unit-тесты сервисов — на Mockito, без БД.
- Интеграционные тесты — на Testcontainers (поднимают реальный Postgres 16),
  поэтому для `mvn verify` нужен **запущенный Docker**.
- Порог покрытия JaCoCo — 70% по строкам, проверяется в фазе `verify`.
  Отчёт: `target/site/jacoco/index.html`.

## Доменная модель

| Таблица | Назначение |
|---------|-----------|
| `users` | пользователи, роль строкой, баланс |
| `services` | каталог сервисов рассылки, цена за сообщение |
| `proxy` | прокси, опционально привязанные к сервису-провайдеру |
| `spam_log` | журнал «отправок» со статусом |
| `spam_log_proxy` | M2M: какие прокси использовались в записи журнала |
| `user_service_subscriptions` | M2M с доп. полями: подписки пользователей на сервисы |

### Обязательные типы связей

- **One-to-Many / Many-to-One**: `users (1) → (N) spam_log`.
- **Many-to-Many**: `spam_log (N) ↔ (M) proxy` через `spam_log_proxy`.
- **Many-to-Many с доп. полем**: `users (N) ↔ (M) services` через
  `user_service_subscriptions` (`subscribed_at`, `discount_percent`).

Все enum хранятся строкой (`@Enumerated(EnumType.STRING)`) и ограничены
`CHECK`-констрейнтами в миграциях.

## REST API

Базовый префикс — `/api/v1`. Полный CRUD для `users`, `services`, `proxy`,
`spam-log`:

| Метод | Путь | Успех | Ошибки |
|-------|------|-------|--------|
| POST | `/{resource}` | 201 | 400, 409 |
| GET | `/{resource}/{id}` | 200 | 404 |
| GET | `/{resource}?page=&size=` | 200 (+ `X-Total-Count`) | 400 |
| PUT | `/{resource}/{id}` | 200 | 400, 404 |
| PATCH | `/{resource}/{id}` | 200 | 400, 404 |
| DELETE | `/{resource}/{id}` | 204 | 404 |

### Пагинация

- **Offset** (`GET /users?page=0&size=20`): тело — список DTO, общее число
  записей — в заголовке `X-Total-Count`. `size` ограничен `@Max(50)`.
- **Cursor** (`GET /spam-log/feed?cursor={uuid}&size=20`): тело —
  `{ "items": [...], "nextCursor": "..." }`. Общее количество **не возвращается**
  (бесконечный скролл). Когда записей больше нет, `nextCursor` = `null`.

### Формат ошибки

```json
{
  "timestamp": "2026-09-24T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/users",
  "details": [
    { "field": "email", "message": "must be a well-formed email address" }
  ]
}
```

## Транзакционные эндпоинты

### `POST /api/v1/spam/send`

Шаги (одна транзакция): найти пользователя и сервис → посчитать стоимость
`price_per_message * messageCount` → проверить баланс → списать → создать
`spam_log` (QUEUED) → привязать прокси (M2M) → **сделать HTTP-POST на приёмник
`spamer.sender.target-url` по каждому сообщению** → перевести в SENT.

Ответ содержит реальный результат: `delivered` (сколько реально принято),
`target` (куда слали) и `lastHttpStatus` (код последнего ответа). Если приёмник
вернул не-2xx или недоступен — бросается ошибка, транзакция откатывается, баланс
не списывается.

**Почему транзакция:** это финансовая операция вместе с журналированием. При
ошибке на любом шаге нужен полный rollback — иначе можно списать деньги без
записи в журнале либо создать «зависшую» запись, за которую не заплатили.

> Нюанс честности: уже принятый HTTP-запрос «отменить» откатом нельзя. Если
> сообщение 1 из N ушло, а N-е упало, списание откатится, но первое сообщение
> приёмник уже получил. Для тестового стенда это нормально; в проде I/O выносят
> из транзакции и сверяют результат отдельно. В лабе отправка оставлена внутри
> транзакции намеренно — чтобы демонстрировать откат списания при сбое доставки.

#### Куда слать (свой сервер)

```bash
# по умолчанию — встроенный приёмник на localhost
# свой тестовый сервер:
export SENDER_TARGET_URL=https://my-test-box.example/inbox
```

Встроенный приёмник `POST /api/v1/test-receiver/inbox` просто логирует тело и
отвечает `200 {"received": true, ...}` — чтобы демо работало из коробки.

#### Демо на своём VPS

На чистом VPS подними приёмник (без зависимостей, только stdlib):

```bash
# на VPS:
PORT=9000 python3 demo/receiver.py        # слушает 0.0.0.0:9000, POST /inbox
```

Запусти приложение, указав на него:

```bash
export SENDER_TARGET_URL=http://<IP-твоего-VPS>:9000/inbox
docker compose up --build
```

Прогон одной командой (создаёт данные и шлёт 3 сообщения):

```bash
BASE=http://localhost:8080 ./demo/demo.sh
```

В ответе `/spam/send` увидишь `delivered/target/lastHttpStatus`, а в логе
`receiver.py` на VPS — три входящих сообщения. `SENDER_TARGET_URL` нигде не
коммитится; IP задаёшь только в своём окружении.

### `POST /api/v1/subscriptions`

Шаги (одна транзакция): найти пользователя и сервис → проверить отсутствие
дубликата подписки → создать запись в `user_service_subscriptions` с
`discount_percent` → если пользователь был `USER_FREE`, сменить роль на
`USER_PRO`.

**Почему транзакция:** создание M2M-записи и смена роли должны быть
консистентны. Иначе возможны состояния «PRO без подписки» или «подписка есть,
а роль не сменилась».

## Пример

```bash
# создать пользователя
curl -s -X POST localhost:8080/api/v1/users -H 'Content-Type: application/json' \
  -d '{"username":"alice","email":"alice@example.com","role":"USER_FREE","balance":"100.00"}'

# создать сервис
curl -s -X POST localhost:8080/api/v1/services -H 'Content-Type: application/json' \
  -d '{"name":"bulk","description":"demo","pricePerMessage":"2.0000"}'

# подписка (переведёт alice в USER_PRO)
curl -s -X POST localhost:8080/api/v1/subscriptions -H 'Content-Type: application/json' \
  -d '{"userId":"<USER_ID>","serviceId":"<SERVICE_ID>","discountPercent":"10.00"}'

# "отправка" (спишет баланс, создаст запись в spam_log)
curl -s -X POST localhost:8080/api/v1/spam/send -H 'Content-Type: application/json' \
  -d '{"userId":"<USER_ID>","serviceId":"<SERVICE_ID>","victimContact":"demo@example.com","messageCount":3}'
```

## Структура

```
com.example.spamer
├── config/      # OpenAPI
├── controller/  # REST
├── dto/         # request / response
├── exception/   # обработчик + формат ошибок
├── mapper/      # MapStruct
├── domain/
│   ├── entity/
│   └── repository/
└── service/     # бизнес-логика, @Transactional
```
