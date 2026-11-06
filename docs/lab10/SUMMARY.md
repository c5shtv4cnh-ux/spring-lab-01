# Lab 10 — Spring Security — summary

Student: Askarkhan Baipanuly (git user.name) · group: not found in the repo · variant: 1 (custom login page; assumed, no variant number was given)
Project: /Users/askarhan/Downloads/spring-lab-01, branch `lab10` (from `lab05`), pushed to
https://github.com/c5shtv4cnh-ux/spring-lab-01/tree/lab10 (commits 8e6a32c, b9b3f68)

Report: `Lab10_Report_Baipanuly.docx` (all tables, listings, 11 screenshots, conclusions, defence answers).

## Environment
Spring Boot 4.1.1 / Spring Security 7.1.1, OpenJDK 21, PostgreSQL 16.15 (installed with Homebrew, `brew services start postgresql@16`), db `springlab`.
The repo had no JPA/Flyway (labs 6–9 not in this project), so JPA + Flyway + PostgreSQL were added here and the
security migration is `V1__security.sql` (content identical to the assignment's V5__security.sql).

## Results
- Task 1 (task1.txt): no config → browser 302 /login, curl 401 Basic; user `user`, generated password in log; own chain → /nowhere 403 (denyAll).
- Task 2 (task2.txt): hashes differ per run; length 60, prefix $2a$10$; enabled=false → login refused (/login?error), back to true → works.
- Task 3 (task3.txt), matrix:

| Request | anonymous | user | admin |
|---|---|---|---|
| GET / | 200 | 200 | 200 |
| GET /books | 302 → /login | 200 | 200 |
| POST /books | 302 → /login | 403 | 302 → /books |
| GET /admin | 302 → /login | 403 | 200 |
| GET /nowhere | 302 → /login | 403 | 403 |

  `./mvnw test`: 5 tests, 0 failures (BookServiceMethodSecurityTests proves @PreAuthorize without HTTP).
- Task 4 (task4.txt): login POST without token 403; with token 302; POST /books without token 403; with token 302 (book created).
- Variant 1 (variant1.txt): own /login template, ?error and ?logout messages.

## Files
task*.txt, variant1.txt — raw curl/psql outputs · screenshots/ — 11 PNG · src/ — changed sources + pom.diff · run*.log — app logs · bin/ — helper scripts used.

## Run for the demo
    cd ~/Downloads/spring-lab-01 && ./mvnw spring-boot:run      # PostgreSQL must be running: brew services start postgresql@16
    users: user / user123, admin / admin123
