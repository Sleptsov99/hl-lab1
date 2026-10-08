#!/usr/bin/env python3
"""Минимальный приёмник для демо лабы.

Поднимается на твоём VPS, слушает порт, логирует каждое входящее сообщение
от /spam/send и отвечает 200. Никаких зависимостей - только stdlib.

Запуск:
    python3 receiver.py            # слушает 0.0.0.0:9000
    PORT=8081 python3 receiver.py  # другой порт

Потом на стороне приложения:
    export SENDER_TARGET_URL=http://<IP-этого-VPS>:9000/inbox
"""
import json
import os
from datetime import datetime, timezone
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

PORT = int(os.environ.get("PORT", "9000"))
_seq = 0


class Handler(BaseHTTPRequestHandler):
    # /spam/send шлёт POST на /inbox; принимаем любой путь, чтобы не спотыкаться
    def do_POST(self):
        global _seq
        length = int(self.headers.get("Content-Length", 0))
        raw = self.rfile.read(length) if length else b""
        try:
            body = json.loads(raw) if raw else {}
        except json.JSONDecodeError:
            body = {"_raw": raw.decode("utf-8", "replace")}

        _seq += 1
        ts = datetime.now(timezone.utc).isoformat(timespec="seconds")
        print(f"[{ts}] #{_seq} {self.path} from {self.client_address[0]} -> {body}",
              flush=True)

        reply = json.dumps({"received": True, "seq": _seq, "at": ts}).encode()
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(reply)))
        self.end_headers()
        self.wfile.write(reply)

    def do_GET(self):  # удобно пингнуть из браузера, что сервер жив
        self.send_response(200)
        self.end_headers()
        self.wfile.write(b"receiver is up\n")

    def log_message(self, *args):
        pass  # свой лог выше, дефолтный шум не нужен


if __name__ == "__main__":
    print(f"receiver listening on 0.0.0.0:{PORT} (POST /inbox)", flush=True)
    ThreadingHTTPServer(("0.0.0.0", PORT), Handler).serve_forever()
