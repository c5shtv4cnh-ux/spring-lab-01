# Lab 12 — evidence

Student: Askarkhan Baipanuly, group 2411. Variant 3: `AuthorService`, branch «an author with no books is deletable»,
extra requirement «a `@Nested` class grouping the failure cases».
Branch `lab12` was created from `lab10` (this repository has no `lab11`; labs 6–9 and 11 are not in it).
Docker: Colima 0.10.3 (Docker Engine 29.5.2) on macOS (`docker info` OK). Database everywhere: PostgreSQL — no H2 anywhere.

## `./mvnw clean verify` — summary line

```
[INFO] Tests run: 28, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

| Test class | Kind | Tests |
|---|---|---|
| `author.AuthorServiceTest` (+ `@Nested WhenTheRequestCannotBeServed`) | unit, Mockito, no Spring | 9 (6 methods, one `@ParameterizedTest` × 3) |
| `web.AuthorControllerTest` | `@WebMvcTest` + `@MockitoBean`, security ON | 4 |
| `author.AuthorRepositoryTest` | `@DataJpaTest` + `TestEntityManager`, PostgreSQL container | 2 |
| `author.AuthorApiIntegrationTest` | `@SpringBootTest(RANDOM_PORT)` + `@Testcontainers` + `@ServiceConnection` + `RestTestClient` | 2 |
| older: `BookServiceMethodSecurityTests`, `AccessDeniedPageTests`, `SpringLab01ApplicationTests` | lab 10 | 11 |

Container in the log (integration test):
```
tc.postgres:17-alpine : Creating container for image: postgres:17-alpine
tc.postgres:17-alpine : Container postgres:17-alpine started in PT0.909322S
tc.postgres:17-alpine : Container is started (JDBC URL: jdbc:postgresql://localhost:32772/test?loggerLevel=OFF)
o.f.c.i.command.DbMigrate : Successfully applied 2 migrations to schema "public", now at version v2
```

## Coverage — package `kz.iitu.spring_lab_01.author`

| | Lines | Branches |
|---|---|---|
| before the extra test | 44 / 45 = 97.8 % | 6 / 10 = 60 % |
| after the extra test | 45 / 45 = 100 % | 8 / 10 = 80 % |

`AuthorService`: lines 15/16 → 16/16, branches 6/10 → 8/10.

Yellow line before (`AuthorService.java`, line 27, «2 of 4 branches missed»), line 28 red:
```java
27  if (query == null || query.isBlank()) {          // yellow: only the "real query" way was taken
28      return authors.findAllByOrderByNameAsc();       // red: never executed
```
The test that turned it green (`AuthorServiceTest`):
```java
@ParameterizedTest
@NullAndEmptySource
@ValueSource(strings = {"   "})
void aMissingOrBlankQueryListsEveryAuthorSortedByName(String query) { ... }
```
After: line 27 «All 4 branches covered», line 28 green.
Screenshots: `docs/lab12/screenshots/05_jacoco_before_AuthorService.png`, `06_jacoco_after_AuthorService.png`.

Still uncovered: line 35 of `create`, `country == null || country.isBlank() ? null : country.strip()` — the
`null`/blank country way is never taken. It should be **tested, not deleted**: it decides whether an empty
country is stored as `NULL` or as an empty string, which is visible to every API client.

## Experiment for checklist item 8 (`em.flush()` removed)
Both tests still pass. The `Author` id uses `GenerationType.IDENTITY`, so Hibernate must run the `INSERT`
already in `persist()` to learn the id (the log shows `insert into author` before the `select`), and a JPQL/derived
query also triggers an automatic flush. With a `SEQUENCE` id the inserts would wait for the flush; then
`em.clear()` would throw them away and the positive test would fail — that is why `flush()` + `clear()` stay.

## Security in the slice and integration tests
* `@WebMvcTest`: the real `SecurityConfiguration` is imported and stays on; requests carry `@WithMockUser`
  (USER or ADMIN) and `.with(csrf())` for POST. A USER posting gets 403 — this is tested too.
* Integration test: real form login as `admin / admin123` (the user from the Flyway migration), then the
  `JSESSIONID` cookie and the `X-CSRF-TOKEN` header. Security was not switched off anywhere.
