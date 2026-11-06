# Spring Transactions and Schema Versioning — summary

Student: Askarkhan Baipanuly, group 2411. Branch `lab-transactions` (from `main`):
https://github.com/c5shtv4cnh-ux/spring-lab-01/tree/lab-transactions

Report: `Lab_Transactions_Report_Baipanuly.docx`. Stack: Spring Boot 4.1.1, Java 21, PostgreSQL 16.15 (db `txlab`), Flyway 12.4.0.

| Task | Result |
|---|---|
| A | transfer 100 → 900/600; 5000 → rollback; RuntimeException after flushed debit → rollback; checked exception → COMMITTED (900/500); rollbackFor → rollback |
| B | REQUIRED: audit row rolled back with the outer tx; REQUIRES_NEW: audit row survives |
| C (PostgreSQL) | READ_COMMITTED: 1000 → 1050 (non-repeatable read); REPEATABLE_READ: 1000 → 1000; RR update of a concurrently changed row → "could not serialize access due to concurrent update" |
| D | stale @Version copy → ObjectOptimisticLockingFailureException, first update kept; reload-and-retry shown |
| E | V1–V3 applied on empty DB, "up to date" on restart, V4 (index) applied later; editing applied V2 → checksum mismatch, app does not start |

`./mvnw test`: 15 tests, 0 failures. Files: task*.txt (evidence), screenshots/ (7 PNG), logs/, src/.
Run: `brew services start postgresql@16` then `./mvnw test` in ~/Downloads/spring-lab-01 (branch lab-transactions).
