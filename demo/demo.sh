#!/usr/bin/env bash
# Демо для защиты: создаёт пользователя, сервис, прокси и дёргает /spam/send.
# Приложение должно быть уже запущено и указывать SENDER_TARGET_URL на приёмник.
#
#   BASE=http://localhost:8080 ./demo/demo.sh
#
set -euo pipefail
BASE="${BASE:-http://localhost:8080}/api/v1"

jqid() { python3 -c "import sys,json; print(json.load(sys.stdin)['id'])"; }
say()  { printf '\n=== %s ===\n' "$1"; }

say "health"
curl -fsS "${BASE%/api/v1}/actuator/health"; echo

say "создаём пользователя (balance=100, PRO)"
USER_ID=$(curl -fsS -X POST "$BASE/users" -H 'Content-Type: application/json' \
  -d '{"username":"demo-'"$RANDOM"'","email":"demo-'"$RANDOM"'@example.com","role":"USER_PRO","balance":"100.00"}' | tee /dev/stderr | jqid)

say "создаём сервис (цена 2 за сообщение)"
SVC_ID=$(curl -fsS -X POST "$BASE/services" -H 'Content-Type: application/json' \
  -d '{"name":"bulk-'"$RANDOM"'","description":"demo","pricePerMessage":"2.0000"}' | tee /dev/stderr | jqid)

say "добавляем активный прокси (для M2M-связи в журнале)"
curl -fsS -X POST "$BASE/proxy" -H 'Content-Type: application/json' \
  -d '{"host":"10.0.0.1","port":8080,"protocol":"HTTP","status":"ACTIVE"}' >/dev/null
echo "ok"

say "РЕАЛЬНАЯ ОТПРАВКА: 3 сообщения на SENDER_TARGET_URL"
curl -fsS -X POST "$BASE/spam/send" -H 'Content-Type: application/json' \
  -d '{"userId":"'"$USER_ID"'","serviceId":"'"$SVC_ID"'","victimContact":"demo@example.com","messageBody":"hello from lab","messageCount":3}' \
  | python3 -m json.tool

say "баланс после отправки (должен стать 94)"
curl -fsS "$BASE/users/$USER_ID" | python3 -c "import sys,json; print('balance =', json.load(sys.stdin)['balance'])"

echo
echo "Смотри лог приёмника на VPS — там должны появиться 3 входящих сообщения."
