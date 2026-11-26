#!/usr/bin/env bash
# Lab 13, task 1: cache hit, eviction on update, the cache seen from outside.
# Usage: docs/lab13/bin/task1_cache.sh <application log file>   (the app must be running on :8080)
LOG=${1:-docs/lab13/raw/app-default.log}
JAR=$(mktemp); B=http://localhost:8080

step() {   # run a command, then show the SQL the application logged while serving it
  echo "\$ $1"; local n; n=$(wc -l < "$LOG")
  eval "$1"; echo; sleep 0.5
  echo "--- SQL logged by the application for this request:"
  tail -n +$((n+1)) "$LOG" | grep "org.hibernate.SQL" | grep -v " loan " \
    | sed -E 's/^.{11}([0-9:.]{12}).*\[ *([^]]*)\] org.hibernate.SQL *: /\1 [\2] /' || true
  echo "(end)"; echo
}

# one admin session via the form login (so later requests carry only the cookie: no user SELECTs
# in the log), and the CSRF token of that session for the PUT
csrf_of() { sed -nE 's/.*name="_csrf"[^>]*value="([^"]+)".*/\1/p' | head -1; }
LOGIN_TOKEN=$(curl -s -c "$JAR" -b "$JAR" $B/login | csrf_of)
curl -s -c "$JAR" -b "$JAR" -o /dev/null $B/login --data-urlencode username=admin \
     --data-urlencode password=admin123 --data-urlencode _csrf="$LOGIN_TOKEN"
TOKEN=$(curl -s -c "$JAR" -b "$JAR" $B/books | csrf_of)

step "curl -s -b $JAR $B/api/authors/4"
step "curl -s -b $JAR $B/api/authors/4"
step "curl -s -b $JAR -X PUT $B/api/authors/4 -H 'Content-Type: application/json' -H 'X-CSRF-TOKEN: $TOKEN' -d '{\"name\":\"Frank Patrick Herbert\",\"country\":\"USA\"}'"
step "curl -s -b $JAR $B/api/authors/4"
step "curl -s -b $JAR $B/api/authors/4"
step "curl -s -b $JAR $B/actuator/caches"
step "curl -s -b $JAR '$B/actuator/metrics/cache.gets?tag=cache:authors&tag=result:hit'"
step "curl -s -b $JAR '$B/actuator/metrics/cache.gets?tag=cache:authors&tag=result:miss'"
# put the original name back (evicts again)
curl -s -b "$JAR" -X PUT $B/api/authors/4 -H 'Content-Type: application/json' -H "X-CSRF-TOKEN: $TOKEN" \
     -d '{"name":"Frank Herbert","country":"USA"}' -o /dev/null
rm -f "$JAR"
