package kz.iitu.spring_lab_01.web;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Lab 13, task 4: an endpoint that does nothing but wait 400 ms (variant 3), so a load test measures
 * only the effect of threads. It exists only in the "perf" profile, and only that profile permits it
 * in the security chain (lab13.permit-slow=true) — the default/merged configuration does not expose it.
 */
@RestController
@Profile("perf")
public class SlowController {

    static final long DELAY_MS = 400;

    @GetMapping("/api/slow")
    Map<String, Object> slow() throws InterruptedException {
        Thread.sleep(DELAY_MS);
        return Map.of("ok", true, "thread", Thread.currentThread().toString());
    }
}
