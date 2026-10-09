#!/usr/bin/env bash
# Демо для защиты: создаёт пользователя, сервис, прокси и дёргает /spam/send.
# Приложение должно быть уже запущено и указывать SENDER_TARGET_URL на приёмник.
#
#   BASE=http://localhost:8080 ./demo/demo.sh
#
set -euo pipefail
BASE="${BASE:-http://localhost:8080}/api/v1"
# сколько сообщений слать: ./demo/demo.sh 100  (или COUNT=100 ./demo/demo.sh)
COUNT="${1:-${COUNT:-3}}"
PRICE=2
# баланса даём с запасом под COUNT*PRICE, чтобы не упереться в 422
BALANCE=$(( COUNT * PRICE + 50 ))

jqid() { python3 -c "import sys,json; print(json.load(sys.stdin)['id'])"; }
say()  { printf '\n=== %s ===\n' "$1"; }

say "health"
curl -fsS "${BASE%/api/v1}/actuator/health"; echo

say "создаём пользователя (balance=$BALANCE, PRO)"
USER_ID=$(curl -fsS -X POST "$BASE/users" -H 'Content-Type: application/json' \
  -d '{"username":"demo-'"$RANDOM"'","email":"demo-'"$RANDOM"'@example.com","role":"USER_PRO","balance":"'"$BALANCE"'.00"}' | tee /dev/stderr | jqid)

say "создаём сервис (цена $PRICE за сообщение)"
SVC_ID=$(curl -fsS -X POST "$BASE/services" -H 'Content-Type: application/json' \
  -d '{"name":"bulk-'"$RANDOM"'","description":"demo","pricePerMessage":"'"$PRICE"'.0000"}' | tee /dev/stderr | jqid)

say "добавляем активный прокси (для M2M-связи в журнале)"
curl -fsS -X POST "$BASE/proxy" -H 'Content-Type: application/json' \
  -d '{"host":"10.0.0.1","port":8080,"protocol":"HTTP","status":"ACTIVE"}' >/dev/null
echo "ok"

say "РЕАЛЬНАЯ ОТПРАВКА: $COUNT сообщ. на SENDER_TARGET_URL"
curl -fsS -X POST "$BASE/spam/send" -H 'Content-Type: application/json' \
  -d '{"userId":"'"$USER_ID"'","serviceId":"'"$SVC_ID"'","victimContact":"demo@example.com","messageBody":"hello from lab","messageCount":'"$COUNT"'}' \
  | python3 -m json.tool

say "баланс после отправки (должен стать $(( BALANCE - COUNT * PRICE )))"
curl -fsS "$BASE/users/$USER_ID" | python3 -c "import sys,json; print('balance =', json.load(sys.stdin)['balance'])"

echo
echo "Смотри лог приёмника на VPS — там должны появиться 3 входящих сообщения."
