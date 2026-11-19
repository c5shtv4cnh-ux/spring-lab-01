package kz.iitu.spring_lab_01.author;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lab 12, task 4: the whole application on a random port against a real PostgreSQL in a container.
 * Flyway runs V1 (users) and V2 (authors) on an empty database; the test logs in through the real
 * form login (admin / admin123 from V1) and sends the session cookie and the CSRF token.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureRestTestClient
class AuthorApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    private static final Pattern CSRF = Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"");
    private static final Pattern SESSION = Pattern.compile("JSESSIONID=([^;]+)");

    @Autowired RestTestClient client;

    private String session;
    private String csrf;

    @BeforeEach
    void logInAsAdmin() {
        // 1. GET /login: an anonymous session and a CSRF token for the login form
        EntityExchangeResult<String> loginPage = client.get().uri("/login").exchange()
                .expectStatus().isOk().expectBody(String.class).returnResult();
        String anonymousSession = cookie(loginPage);
        String loginToken = token(loginPage.getResponseBody());

        // 2. POST /login with the token: a new authenticated session
        EntityExchangeResult<Void> login = client.post().uri("/login")
                .header(HttpHeaders.COOKIE, "JSESSIONID=" + anonymousSession)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("username=admin&password=admin123&_csrf=" + loginToken)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches(HttpHeaders.LOCATION, ".*/books$")
                .expectBody().isEmpty();
        session = cookie(login);

        // 3. a page of the logged-in session carries the CSRF token for further POST/DELETE requests
        String booksPage = client.get().uri("/books").header(HttpHeaders.COOKIE, "JSESSIONID=" + session)
                .exchange().expectStatus().isOk().expectBody(String.class).returnResult().getResponseBody();
        csrf = token(booksPage);
    }

    @Test
    void createsAnAuthorThenFindsItBySearch() {
        client.post().uri("/api/authors")
              .header(HttpHeaders.COOKIE, "JSESSIONID=" + session)
              .header("X-CSRF-TOKEN", csrf)
              .contentType(MediaType.APPLICATION_JSON)
              .body("{\"name\":\"Ursula K. Le Guin\",\"country\":\"USA\"}")
              .exchange()
              .expectStatus().isCreated()
              .expectHeader().valueMatches(HttpHeaders.LOCATION, "/api/authors/\\d+");

        client.get().uri("/api/authors?q=le guin")
              .header(HttpHeaders.COOKIE, "JSESSIONID=" + session)
              .exchange()
              .expectStatus().isOk()
              .expectBody()
              .jsonPath("$.length()").isEqualTo(1)
              .jsonPath("$[0].name").isEqualTo("Ursula K. Le Guin");
    }

    @Test
    void anAuthorWithBooksIsRefusedAndAnAuthorWithoutBooksIsDeleted() {
        // V2 seeds Joshua Bloch (id 1, has a book in the catalogue) and Frank Herbert (id 4, no books)
        client.delete().uri("/api/authors/1")
              .header(HttpHeaders.COOKIE, "JSESSIONID=" + session).header("X-CSRF-TOKEN", csrf)
              .exchange()
              .expectStatus().isEqualTo(409)
              .expectBody().jsonPath("$.title").isEqualTo("Author still has books");

        client.delete().uri("/api/authors/4")
              .header(HttpHeaders.COOKIE, "JSESSIONID=" + session).header("X-CSRF-TOKEN", csrf)
              .exchange()
              .expectStatus().isNoContent();

        client.get().uri("/api/authors/4")
              .header(HttpHeaders.COOKIE, "JSESSIONID=" + session)
              .exchange()
              .expectStatus().isNotFound()
              .expectBody().jsonPath("$.detail").isEqualTo("Author 4 not found");
    }

    private static String cookie(EntityExchangeResult<?> result) {
        String header = String.join(";", result.getResponseHeaders().getOrEmpty(HttpHeaders.SET_COOKIE));
        Matcher m = SESSION.matcher(header);
        assertThat(m.find()).as("Set-Cookie with JSESSIONID").isTrue();
        return m.group(1);
    }

    private static String token(String html) {
        Matcher m = CSRF.matcher(html);
        assertThat(m.find()).as("hidden _csrf field in the page").isTrue();
        return m.group(1);
    }
}
