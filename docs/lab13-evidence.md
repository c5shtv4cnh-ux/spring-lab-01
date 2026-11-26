# Lab 13 — evidence

Student: Askarkhan Baipanuly, group 2411. **Variant 3**: cached read `AuthorService.byId` · Caffeine;
@Async «welcome e-mail on signup»; @Scheduled «nightly overdue marking, cron 02:00»; load test **400 ms · pool 25 · concurrency 200**.
Branch `lab13` from `lab12`. Machine: Apple M3, 8 cores, macOS; OpenJDK 21.0.12; Spring Boot 4.1.1; PostgreSQL 16; oha 1.17.0.
Raw outputs: `docs/lab13/raw/`, scripts that produced them: `docs/lab13/bin/`.

## Before measuring: the ceiling (Little's law, L = λ·W)

* MVC, platform threads: at most 25 requests are in service at once (L = 25), each holds its thread W = 0.4 s →
  **λ_max = 25 / 0.4 = 62.5 req/s**. With 200 clients the rest wait in Tomcat's queue, so a client sees
  W = 200 / 62.5 = **3.2 s**.
* Virtual threads / Mono.delay: the thread is no longer the limit, the 200 clients are: λ_max = 200 / 0.4 = **500 req/s**, W ≈ 0.4 s.
* WebFlux with Thread.sleep on the event loop: 8 event-loop threads (`reactor-http-nio-1..8`, = cores) → 8 / 0.4 = **20 req/s**.
* boundedElastic: at most 10 × cores = 80 threads → 80 / 0.4 = **200 req/s**; 200 clients / 80 threads = 2.5 waves → p50 ≈ 0.8 s.

## Results (each: warm-up `oha -n 500 -c 50` discarded, then `oha --no-tui -n 2000 -c 200` three times; median run shown)

| Configuration | Concurrency | req/s (oha) | successful req/s | p50, ms | p99, ms | Errors |
|---|---|---|---|---|---|---|
| MVC, platform threads (pool of 25) | 200 | 61.8 | 61.8 | 3233 | 3249 | 0 |
| MVC, virtual threads | 200 | 476.1 | 470.1 | 420 | 425 | 25 |
| WebFlux, Mono.delay | 200 | 491.1 | 491.1 | 406 | 413 | 0 |
| WebFlux, Thread.sleep on the event loop | 200 | 247.3 | 19.3 | 4046 | 8083 | 1844 |
| WebFlux, sleep on boundedElastic | 200 | 183.5 | 183.5 | 811 | 1614 | 0 |

«req/s (oha)» counts failed requests too; «successful req/s» = 200-responses / total time. Percentiles, not averages.

All measured runs (each after the same warm-up):

| Configuration | Run | req/s (oha) | successful req/s | p50, ms | p99, ms | Errors |
|---|---|---|---|---|---|---|
| MVC, platform threads (pool of 25) | 1 | 62.6 | 61.4 | 3233 | 3580 | 38 |
| MVC, platform threads (pool of 25) | 2 | 61.8 | 61.8 | 3233 | 3249 | 0 |
| MVC, platform threads (pool of 25) | 3 | 61.8 | 61.8 | 3232 | 3242 | 0 |
| MVC, virtual threads | 1 | 471.4 | 459.8 | 422 | 443 | 49 |
| MVC, virtual threads | 2 | 475.5 | 475.5 | 420 | 428 | 0 |
| MVC, virtual threads | 3 | 476.1 | 470.1 | 420 | 425 | 25 |
| WebFlux, Mono.delay | 1 | 491.0 | 491.0 | 405 | 418 | 0 |
| WebFlux, Mono.delay | 2 | 491.1 | 491.1 | 406 | 414 | 0 |
| WebFlux, Mono.delay | 3 | 491.1 | 491.1 | 406 | 413 | 0 |
| WebFlux, Thread.sleep on the event loop | 1 | 247.3 | 19.3 | 4046 | 8083 | 1844 |
| WebFlux, Thread.sleep on the event loop | 2 | 275.1 | 19.3 | 3635 | 7266 | 1860 |
| WebFlux, Thread.sleep on the event loop | 3 | 260.1 | 19.5 | 4049 | 7683 | 1850 |
| WebFlux, sleep on boundedElastic | 1 | 177.0 | 177.0 | 811 | 1616 | 0 |
| WebFlux, sleep on boundedElastic | 2 | 190.8 | 190.8 | 810 | 1610 | 0 |
| WebFlux, sleep on boundedElastic | 3 | 183.5 | 183.5 | 811 | 1614 | 0 |

## Five sentences on the numbers

1. Platform threads gave 61.8 req/s against a computed ceiling of 62.5 (99 %): 25 threads each held for 0.4 s cannot serve more, and by L = λ·W the 200 clients must wait 200 / 61.8 = 3.24 s — exactly the measured p50 of 3233 ms.
2. Virtual threads removed the 25-thread limit, so the limit became the 200 concurrent clients: 470–476 req/s with p50 420 ms, i.e. 94–95 % of the 500 req/s that 200 / 0.4 s predicts; the missing 5 % is the ~20 ms per request of the filter chain, JSON and loopback on top of the 400 ms sleep.
3. WebFlux with Mono.delay reached 491 req/s (p50 406 ms) with only 8 event-loop threads, because during the delay no thread waits — a timer completes the Mono — so it sits even closer to the 500 req/s bound than virtual threads.
4. Thread.sleep on the event loop collapsed to 19.3 successful req/s, as predicted by 8 threads / 0.4 s = 20; worse, while all eight loops sleep nobody accepts or reads sockets, so ~1850 of 2000 requests ended in connection resets — the 247–275 «req/s» oha prints is mostly fast failures.
5. Moving the sleep to boundedElastic recovered 177–191 req/s, but only part of 491, because the sleep still holds a real thread and the pool is capped at 80 threads: 80 / 0.4 = 200 req/s, and the 200 clients come in waves of 80 (p50 811 ms ≈ 2 × 400, p99 1614 ms ≈ 4 × 400).

## What surprised me (honest notes)

* **Connection errors at concurrency 200 on macOS.** The first series (with `http://localhost`, kept in `raw/localhost-first-attempt/`) and several runs of the final series had up to 293 «connection reset / connection error» results, also for /slow on Netty. The application logs show `Error setting socket options: java.net.SocketException: Invalid argument` from the acceptor at the moment oha opens its 200 connections at once. Cause: the OS accept queue — `kern.ipc.somaxconn = 128` on this Mac, Tomcat `accept-count = 100` — overflows during the burst (the file-descriptor limit, `ulimit -n` = 1048576, is not the cause). The errors appear only at connection set-up (keep-alive connections are reused afterwards), vary from run to run (0–49 per run in the final series), and are shown, not removed; the median run is reported and all runs are listed. Raising `somaxconn` needs root and was not changed.
* With /blocking the errors are not an artefact but the collapse itself: the event loops that should accept connections are asleep.
* The @Async signup answers in 80 ms, not «a few ms»: the request still BCrypt-hashes the password (cost 10, ~70 ms) on the request thread; the 2-second mail is gone from it.

## Task 1 — cache (`raw/task1_cache.txt`, script `bin/task1_cache.sh`)

* 1st GET /api/authors/4 → `select a1_0.id,a1_0.country,a1_0.name from author a1_0 where a1_0.id=?`; 2nd identical GET → **no SQL**.
* PUT /api/authors/4 (rename) → select + `update author ...`, entry evicted; next GET → **select again**, the GET after it → no SQL.
* `/actuator/caches` → cache `authors`, target `com.github.benmanes.caffeine.cache.BoundedLocalCache$BoundedLocalManualCache`;
  `/actuator/metrics/cache.gets` → hit = 2, miss = 2 (exactly the four GETs above). The cached value is `AuthorDto` (record), never the entity.
* Write paths: `update` and `delete` carry `@CacheEvict(key = "#id")` (`create` makes a new id, nothing to evict). Unit test `AuthorServiceCacheTest` (3 tests).

## Task 2 — @Async (`raw/task2_async.txt`)

| Endpoint | time_total | Threads in the log |
|---|---|---|
| POST /api/signup (through the proxy) | **0.081 s** | `http-nio-8080-exec-2` (controller) → `async-2` (e-mail, 2 s later «sent») |
| POST /api/signup/self-invocation (`this.welcomeEmail`) | **2.091 s** | `http-nio-8080-exec-4` for both — @Async ignored |

Executor: `async-`, core 4, max 8, queue 50. The method receives the user id and loads the user itself (outside the caller's transaction).

## Task 3 — @Scheduled (`raw/task3_scheduled.txt`)

`@Scheduled(cron = "${library.overdue.cron:0 0 2 * * *}", zone = "Asia/Almaty")`, one UPDATE in a transaction.
Demo cron `0/20 * * * * *`: runs at :00/:20/:40 on `sched-1`, a new overdue loan is marked at 01:45:00.
**Real nightly run** with the default expression: `02:00:00.217 [sched-1] overdue marking for 2026-11-27: 1 loan(s) newly marked, 4 overdue in total`.

## Task 4 — thread field (`raw/task4_thread_field.txt`)

* profile `perf`: `Thread[#55,http-nio-8080-exec-13,5,main]` — platform thread from the 25-thread pool
* profiles `perf,vt`: `VirtualThread[#52,tomcat-handler-1]/runnable@ForkJoinPool-1-worker-1`

`/api/slow` exists only in the `perf` profile (`@Profile("perf")`), and only that profile sets `lab13.permit-slow=true`;
in the default configuration it is not permitted (`anyRequest().denyAll()`).

## Task 5 — reactive-demo

`reactive-demo/`: Java 21, only `spring-boot-starter-webflux` → `Netty started on port 8081 (http)`. Threads seen:
/slow → `parallel-1` (the timer), /blocking → `reactor-http-nio-3` (an event loop), /blocking-fixed → `boundedElastic-1`.
