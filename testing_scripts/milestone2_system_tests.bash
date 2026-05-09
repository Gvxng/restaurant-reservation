#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
RUN_SUFFIX="$(date +%s)-${RANDOM}"
CUSTOMER_ID=$((900000 + RANDOM))
TEST_DATE="2031-06-10"

TMP_DIR="$(mktemp -d)"
LAST_BODY="${TMP_DIR}/last-response.json"
trap 'rm -rf "${TMP_DIR}"' EXIT

pass() {
  printf 'PASS: %s\n' "$1"
}

fail() {
  printf 'FAIL: %s\n' "$1" >&2
  printf 'Last response body:\n' >&2
  cat "${LAST_BODY}" >&2 || true
  printf '\n' >&2
  exit 1
}

request() {
  local method="$1"
  local path="$2"
  local expected_status="$3"
  local body="${4:-}"
  local status

  if [[ -n "${body}" ]]; then
    status="$(curl -sS -o "${LAST_BODY}" -w '%{http_code}' \
      -X "${method}" "${BASE_URL}${path}" \
      -H 'Content-Type: application/json' \
      -d "${body}")"
  else
    status="$(curl -sS -o "${LAST_BODY}" -w '%{http_code}' \
      -X "${method}" "${BASE_URL}${path}")"
  fi

  if [[ "${status}" != "${expected_status}" ]]; then
    fail "${method} ${path} expected ${expected_status}, got ${status}"
  fi
}

json_value() {
  local key="$1"
  local file="${2:-${LAST_BODY}}"

  if command -v jq >/dev/null 2>&1; then
    jq -r ".${key}" "${file}"
  elif command -v python3 >/dev/null 2>&1; then
    python3 -c 'import json,sys; print(json.load(open(sys.argv[2]))[sys.argv[1]])' "${key}" "${file}"
  elif command -v python >/dev/null 2>&1; then
    python -c 'import json,sys; print(json.load(open(sys.argv[2]))[sys.argv[1]])' "${key}" "${file}"
  else
    sed -n "s/.*\"${key}\"[[:space:]]*:[[:space:]]*\"*\\([^\",}]*\\)\"*.*/\\1/p" "${file}" | head -n 1
  fi
}

printf 'Running Milestone 2 system integration checks through %s\n' "${BASE_URL}"

request GET "/api/v1/menu-items" 200
pass "GET menu downstream through API Gateway"

request POST "/api/v1/menu-items" 201 "{
  \"menuId\": 1,
  \"name\": \"MS2 Available Item ${RUN_SUFFIX}\",
  \"description\": \"Created by Milestone 2 system script\",
  \"amount\": 9.75,
  \"currency\": \"CAD\",
  \"category\": \"APPETIZER\",
  \"available\": true,
  \"dietaryTags\": \"TEST\"
}"
AVAILABLE_MENU_ITEM_ID="$(json_value menuItemId)"
pass "POST menu downstream through API Gateway created menu item ${AVAILABLE_MENU_ITEM_ID}"

request POST "/api/v1/menu-items" 201 "{
  \"menuId\": 1,
  \"name\": \"MS2 Unavailable Item ${RUN_SUFFIX}\",
  \"description\": \"Used to test aggregate invariant failure\",
  \"amount\": 7.25,
  \"currency\": \"CAD\",
  \"category\": \"APPETIZER\",
  \"available\": false,
  \"dietaryTags\": \"TEST\"
}"
UNAVAILABLE_MENU_ITEM_ID="$(json_value menuItemId)"
pass "Created unavailable menu item ${UNAVAILABLE_MENU_ITEM_ID} for negative aggregate test"

request GET "/api/v1/loyalty-accounts" 200
pass "GET loyalty downstream through API Gateway"

request POST "/api/v1/loyalty-accounts" 201 "{
  \"customerId\": ${CUSTOMER_ID},
  \"pointsBalance\": 0,
  \"tier\": \"BRONZE\",
  \"enrollmentDate\": \"$(date +%F)\"
}"
LOYALTY_ACCOUNT_ID="$(json_value accountId)"
pass "POST loyalty downstream through API Gateway created loyalty account ${LOYALTY_ACCOUNT_ID}"

request GET "/api/v1/dining-tables" 200
pass "GET reservation downstream through API Gateway"

request POST "/api/v1/dining-tables" 201 "{
  \"tableNumber\": \"MS2-${RUN_SUFFIX}\",
  \"seatingCapacity\": 4,
  \"tableType\": \"INDOOR\",
  \"status\": \"AVAILABLE\",
  \"sectionId\": 1,
  \"positionX\": 10,
  \"positionY\": 10
}"
TABLE_ID="$(json_value tableId)"
pass "POST reservation downstream through API Gateway created dining table ${TABLE_ID}"

request GET "/api/v1/reservations" 200
pass "GET reservation aggregates through API Gateway"

request POST "/api/v1/reservations" 201 "{
  \"customerId\": ${CUSTOMER_ID},
  \"tableId\": ${TABLE_ID},
  \"reservationDate\": \"${TEST_DATE}\",
  \"timeSlotStart\": \"18:00:00\",
  \"timeSlotEnd\": \"20:00:00\",
  \"partySize\": 4,
  \"status\": \"CONFIRMED\",
  \"preOrderItems\": [
    {\"menuItemId\": ${AVAILABLE_MENU_ITEM_ID}, \"quantity\": 2}
  ]
}"
AGGREGATE_ID="$(json_value aggregateId)"
BOOKING_ID="$(json_value bookingId)"
PRE_ORDER_ID="$(json_value preOrderId)"
pass "POST reservation aggregate created aggregate ${AGGREGATE_ID}, booking ${BOOKING_ID}, pre-order ${PRE_ORDER_ID}"

request GET "/api/v1/reservations/${AGGREGATE_ID}" 200
pass "GET reservation aggregate by aggregateId"

request PUT "/api/v1/reservations/${AGGREGATE_ID}" 200 "{
  \"customerId\": ${CUSTOMER_ID},
  \"tableId\": ${TABLE_ID},
  \"reservationDate\": \"${TEST_DATE}\",
  \"timeSlotStart\": \"18:00:00\",
  \"timeSlotEnd\": \"20:00:00\",
  \"partySize\": 4,
  \"status\": \"CONFIRMED\",
  \"preOrderItems\": [
    {\"menuItemId\": ${AVAILABLE_MENU_ITEM_ID}, \"quantity\": 3}
  ]
}"
pass "PUT reservation aggregate recomputed total from menu-service price"

request POST "/api/v1/reservations" 409 "{
  \"customerId\": ${CUSTOMER_ID},
  \"tableId\": ${TABLE_ID},
  \"reservationDate\": \"2031-06-11\",
  \"timeSlotStart\": \"18:00:00\",
  \"timeSlotEnd\": \"20:00:00\",
  \"partySize\": 4,
  \"status\": \"CONFIRMED\",
  \"preOrderItems\": [
    {\"menuItemId\": ${UNAVAILABLE_MENU_ITEM_ID}, \"quantity\": 1}
  ]
}"
pass "Negative aggregate invariant path rejects unavailable menu item"

request DELETE "/api/v1/reservations/${AGGREGATE_ID}" 204
pass "DELETE reservation aggregate"

request GET "/api/v1/reservations/${AGGREGATE_ID}" 404
pass "GET deleted reservation aggregate returns 404"

printf 'All Milestone 2 system integration checks passed through %s\n' "${BASE_URL}"
