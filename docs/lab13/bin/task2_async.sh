#!/usr/bin/env bash
# Lab 13, task 2: @Async welcome e-mail on signup, then the self-invocation mistake.
# Usage: docs/lab13/bin/task2_async.sh <application log file>
LOG=${1:-docs/lab13/raw/app-default.log}; B=http://localhost:8080; S=$(date +%s)

call() {   # $1 = path, $2 = username
  echo "\$ curl -s -w '\\ntime_total=%{time_total}s\\n' -X POST $B$1 -H 'Content-Type: application/json' -d '{\"username\":\"$2\",\"password\":\"secret123\"}'"
  local n; n=$(wc -l < "$LOG")
  curl -s -w '\ntime_total=%{time_total}s\n' -X POST "$B$1" -H 'Content-Type: application/json' \
       -d "{\"username\":\"$2\",\"password\":\"secret123\"}"
  sleep 2.5
  echo "--- application log:"
  tail -n +$((n+1)) "$LOG" | grep -E "SignupController|NotificationService" \
    | sed -E 's/^.{11}([0-9:.]{12}).*\[ *([^]]*)\] ([A-Za-z0-9_.]*\.)?([A-Za-z]+) *: /\1 [\2] \4: /'
  echo
}
call /api/signup                 "reader_$S"
call /api/signup/self-invocation "reader_${S}_self"
