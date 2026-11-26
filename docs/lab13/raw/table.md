| Configuration | Concurrency | req/s (oha) | successful req/s | p50, ms | p99, ms | Errors |
|---|---|---|---|---|---|---|
| MVC, platform threads (pool of 25) | 200 | 61.8 | 61.8 | 3233 | 3249 | 0 |
| MVC, virtual threads | 200 | 476.1 | 470.1 | 420 | 425 | 25 |
| WebFlux, Mono.delay | 200 | 491.1 | 491.1 | 406 | 413 | 0 |
| WebFlux, Thread.sleep on the event loop | 200 | 247.3 | 19.3 | 4046 | 8083 | 1844 |
| WebFlux, sleep on boundedElastic | 200 | 183.5 | 183.5 | 811 | 1614 | 0 |

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
