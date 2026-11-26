#!/usr/bin/env bash
# Lab 13: the same oha commands for every configuration.
# One warm-up (discarded), then three measured runs with -n 2000 -c 200 (variant 3: concurrency 200).
# Usage: docs/lab13/bin/measure.sh <url> <output prefix>      e.g. ... http://127.0.0.1:8080/api/slow docs/lab13/raw/mvc_platform
URL=$1; OUT=$2
echo "# warm-up (discarded): oha --no-tui -n 500 -c 50 $URL"
oha --no-tui -n 500 -c 50 "$URL" > /dev/null
for i in 1 2 3; do
  { echo "\$ oha --no-tui -n 2000 -c 200 $URL"; oha --no-tui -n 2000 -c 200 "$URL"; } > "$OUT.run$i.txt"
  echo "run $i: $(grep -E 'Requests/sec' "$OUT.run$i.txt" | xargs)  ok=$(grep -oE '\[200\] [0-9]+' "$OUT.run$i.txt")  p50=$(grep '50.00%' "$OUT.run$i.txt" | xargs)  p99=$(grep '99.00%' "$OUT.run$i.txt" | xargs)"
  sleep 2
done
