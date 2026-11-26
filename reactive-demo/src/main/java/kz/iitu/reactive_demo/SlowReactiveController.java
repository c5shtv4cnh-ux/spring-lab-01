package kz.iitu.reactive_demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Map;

/**
 * Lab 13, task 5 (variant 3: 400 ms). The same delay implemented three ways.
 */
@RestController
class SlowReactiveController {

    static final long DELAY_MS = 400;

    /** Non-blocking: a timer completes the Mono after 400 ms, no thread waits meanwhile. */
    @GetMapping("/slow")
    Mono<Map<String, Object>> slow() {
        return Mono.delay(Duration.ofMillis(DELAY_MS))
                   .map(t -> Map.of("ok", true, "thread", Thread.currentThread().getName()));
    }

    /** The mistake: blocking code pasted into a reactive app holds an event-loop thread for 400 ms. */
    @GetMapping("/blocking")
    Mono<Map<String, Object>> blocking() throws InterruptedException {
        Thread.sleep(DELAY_MS);                          // blocks the event loop
        return Mono.just(Map.of("ok", true, "thread", Thread.currentThread().getName()));
    }

    /** The fix: the sleep stays, but it runs on Reactor's boundedElastic pool, not on the event loop. */
    @GetMapping("/blocking-fixed")
    Mono<Map<String, Object>> blockingFixed() {
        return Mono.fromCallable(() -> {
                       Thread.sleep(DELAY_MS);
                       return Map.<String, Object>of("ok", true, "thread", Thread.currentThread().getName());
                   })
                   .subscribeOn(Schedulers.boundedElastic());
    }
}
