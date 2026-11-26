package kz.iitu.reactive_demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;

/** Lab 13, task 5: each endpoint answers, and the thread field shows where the work ran. */
@WebFluxTest(SlowReactiveController.class)
class SlowReactiveControllerTest {

    @Autowired WebTestClient client;

    @Test
    void slowCompletesOnATimerThread() {
        client.get().uri("/slow").exchange()
              .expectStatus().isOk()
              .expectBody().jsonPath("$.ok").isEqualTo(true)
                           .jsonPath("$.thread").value(t -> org.assertj.core.api.Assertions.assertThat((String) t).startsWith("parallel-"));
    }

    @Test
    void theFixedEndpointSleepsOnBoundedElastic() {
        client.get().uri("/blocking-fixed").exchange()
              .expectStatus().isOk()
              .expectBody().jsonPath("$.thread").value(t -> org.assertj.core.api.Assertions.assertThat((String) t).startsWith("boundedElastic-"));
    }
}
