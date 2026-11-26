#!/usr/bin/env python3
"""Lab 13: builds the results table from the raw oha outputs (docs/lab13/raw/<config>.runN.txt)."""
import re, sys, pathlib

RAW = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else "docs/lab13/raw")
CONFIGS = [
    ("mvc_platform", "MVC, platform threads (pool of 25)"),
    ("mvc_virtual", "MVC, virtual threads"),
    ("webflux_slow", "WebFlux, Mono.delay"),
    ("webflux_blocking", "WebFlux, Thread.sleep on the event loop"),
    ("webflux_blocking-fixed", "WebFlux, sleep on boundedElastic"),
]

def secs(text, label):
    m = re.search(label + r"\s+([\d.]+) (sec|ms)", text)
    return float(m.group(1)) * (1 if m.group(2) == "sec" else 0.001)

def parse(path):
    t = path.read_text()
    total = secs(t, "Total:")
    ok = int((re.search(r"\[200\] (\d+) responses", t) or [0, 0])[1])
    return dict(rps=float(re.search(r"Requests/sec:\s+([\d.]+)", t).group(1)), total=total, ok=ok,
                ok_rps=ok / total, errors=2000 - ok,
                p50=secs(t, r"50\.00% in") * 1000, p99=secs(t, r"99\.00% in") * 1000)

rows, detail = [], []
for key, name in CONFIGS:
    runs = [parse(p) for p in sorted(RAW.glob(f"{key}.run*.txt"))]
    if not runs:
        continue
    med = sorted(runs, key=lambda r: r["ok_rps"])[len(runs) // 2]
    rows.append(f"| {name} | 200 | {med['rps']:.1f} | {med['ok_rps']:.1f} | {med['p50']:.0f} | {med['p99']:.0f} | {med['errors']} |")
    for i, r in enumerate(runs, 1):
        detail.append(f"| {name} | {i} | {r['rps']:.1f} | {r['ok_rps']:.1f} | {r['p50']:.0f} | {r['p99']:.0f} | {r['errors']} |")

print("| Configuration | Concurrency | req/s (oha) | successful req/s | p50, ms | p99, ms | Errors |")
print("|---|---|---|---|---|---|---|")
print("\n".join(rows))
print("\nAll measured runs (each after the same warm-up):\n")
print("| Configuration | Run | req/s (oha) | successful req/s | p50, ms | p99, ms | Errors |")
print("|---|---|---|---|---|---|---|")
print("\n".join(detail))
