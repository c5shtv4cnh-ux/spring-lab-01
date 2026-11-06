package kz.iitu.spring_lab_01.bank;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

/** Puts the two accounts back to the state of V1 before every test and prints readable evidence. */
abstract class BankTestSupport {

    @Autowired AccountRepository accounts;
    @Autowired AuditRepository audit;
    @Autowired JdbcTemplate jdbc;

    Long alice;
    Long bob;

    @BeforeEach
    void resetData() {
        jdbc.update("update accounts set balance = 1000.00 where owner_name = 'Alice'");
        jdbc.update("update accounts set balance =  500.00 where owner_name = 'Bob'");
        jdbc.update("delete from audit_log");
        alice = accounts.findByOwnerName("Alice").orElseThrow().getId();
        bob = accounts.findByOwnerName("Bob").orElseThrow().getId();
    }

    /** Reads straight from the database, bypassing any persistence context. */
    BigDecimal balance(Long id) {
        return jdbc.queryForObject("select balance from accounts where id = ?", BigDecimal.class, id);
    }

    void show(String step) {
        System.out.printf("%n>>> %s%n>>>     Alice = %s, Bob = %s, audit_log rows = %d%n",
                step, balance(alice), balance(bob), audit.count());
    }

    static void say(String text) {
        System.out.println(">>> " + text);
    }
}
