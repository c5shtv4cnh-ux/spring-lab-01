package kz.iitu.spring_lab_01.bank;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Task D — optimistic locking with @Version. */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TaskDOptimisticLockingTests extends BankTestSupport {

    @Autowired PlatformTransactionManager txManager;

    private Long version(Long id) {
        return jdbc.queryForObject("select version from accounts where id = ?", Long.class, id);
    }

    @Test @Order(1)
    void d1_staleCopyCannotOverwriteTheFirstUpdate() {
        // two independent persistence contexts: every findById outside a transaction gets its own
        Account copy1 = accounts.findById(alice).orElseThrow();
        Account copy2 = accounts.findById(alice).orElseThrow();
        say("D1 copy1 = " + copy1);
        say("   copy2 = " + copy2);

        copy1.credit(new BigDecimal("10.00"));
        accounts.save(copy1);                                     // commit #1
        say("   copy1: credit 10.00 committed -> database balance " + balance(alice) + ", version " + version(alice));

        copy2.debit(new BigDecimal("20.00"));                       // based on the stale balance and version
        assertThatThrownBy(() -> accounts.save(copy2))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class)
                .satisfies(e -> say("   copy2: debit 20.00 REJECTED: " + e.getClass().getSimpleName() + ": "
                        + e.getMessage().lines().findFirst().orElse("")));

        say("   database after the conflict: balance " + balance(alice) + ", version " + version(alice)
                + " (the first update was not overwritten)");
        assertThat(balance(alice)).isEqualByComparingTo("1010.00");
    }

    @Test @Order(2)
    void d2_twoConcurrentTransactions() {
        TransactionTemplate tx = new TransactionTemplate(txManager);
        assertThatThrownBy(() -> tx.executeWithoutResult(s -> {
            Account t1copy = accounts.findById(alice).orElseThrow();
            say("D2 T1 loaded " + t1copy);
            // T2: another thread, so another transaction and persistence context; it commits before T1 continues
            CompletableFuture.runAsync(() -> new TransactionTemplate(txManager).executeWithoutResult(s2 -> {
                Account t2copy = accounts.findById(alice).orElseThrow();
                t2copy.credit(new BigDecimal("5.00"));
            })).join();
            say("   T2 credited 5.00 and committed -> version " + version(alice));
            t1copy.debit(new BigDecimal("30.00"));
            say("   T1 debits 30.00 and commits ...");
        })).isInstanceOf(ObjectOptimisticLockingFailureException.class)
           .satisfies(e -> say("   T1 commit REJECTED: " + e.getClass().getSimpleName()));
        say("   database: balance " + balance(alice) + " (1000 + 5; T1's debit was rolled back)");
        assertThat(balance(alice)).isEqualByComparingTo("1005.00");
    }

    @Test @Order(3)
    void d3_safeResponse_reloadAndRetryOnce() {
        Account stale = accounts.findById(alice).orElseThrow();
        jdbc.update("update accounts set balance = balance + 1.00, version = version + 1 where id = ?", alice);
        say("D3 someone else changed Alice meanwhile -> version " + version(alice));

        stale.credit(new BigDecimal("100.00"));
        try {
            accounts.save(stale);
        } catch (ObjectOptimisticLockingFailureException conflict) {
            say("   conflict detected -> reload the current state and re-apply the operation (a credit is safe to repeat once it was NOT applied)");
            Account fresh = accounts.findById(alice).orElseThrow();
            fresh.credit(new BigDecimal("100.00"));
            accounts.save(fresh);
        }
        say("   database: balance " + balance(alice) + ", version " + version(alice));
        assertThat(balance(alice)).isEqualByComparingTo("1101.00");
    }
}
