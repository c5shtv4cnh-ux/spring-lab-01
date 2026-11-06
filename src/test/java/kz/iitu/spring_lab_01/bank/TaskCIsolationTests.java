package kz.iitu.spring_lab_01.bank;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Task C — isolation on PostgreSQL 16.
 * T1 reads Alice twice; between the two reads T2 (another thread, another connection)
 * adds 50.00 to Alice and commits.
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TaskCIsolationTests extends BankTestSupport {

    @Autowired PlatformTransactionManager txManager;

    private TransactionTemplate tx(int isolation) {
        TransactionTemplate t = new TransactionTemplate(txManager);
        t.setIsolationLevel(isolation);
        return t;
    }

    private BigDecimal readAlice() {
        return jdbc.queryForObject("select balance from accounts where owner_name = 'Alice'", BigDecimal.class);
    }

    /** T2: a separate thread, therefore a separate connection and transaction; it commits before returning. */
    private void concurrentDepositOf50() {
        CompletableFuture.runAsync(() -> tx(TransactionDefinition.ISOLATION_READ_COMMITTED).executeWithoutResult(s ->
                jdbc.update("update accounts set balance = balance + 50.00 where owner_name = 'Alice'"))).join();
        say("    T2: added 50.00 to Alice and COMMITTED");
    }

    @Test @Order(1)
    void c1_readCommitted_secondReadSeesTheCommittedChange() {
        BigDecimal[] reads = tx(TransactionDefinition.ISOLATION_READ_COMMITTED).execute(s -> {
            say("C1 READ_COMMITTED, T1 isolation in the database: " + jdbc.queryForObject("show transaction_isolation", String.class));
            BigDecimal first = readAlice();
            say("    T1 first read : " + first);
            concurrentDepositOf50();
            BigDecimal second = readAlice();
            say("    T1 second read: " + second + "  -> non-repeatable read: T1 sees T2's committed change");
            return new BigDecimal[]{first, second};
        });
        assertThat(reads[0]).isEqualByComparingTo("1000.00");
        assertThat(reads[1]).isEqualByComparingTo("1050.00");
    }

    @Test @Order(2)
    void c2_repeatableRead_secondReadStillSeesTheSnapshot() {
        BigDecimal[] reads = tx(TransactionDefinition.ISOLATION_REPEATABLE_READ).execute(s -> {
            say("C2 REPEATABLE_READ, T1 isolation in the database: " + jdbc.queryForObject("show transaction_isolation", String.class));
            BigDecimal first = readAlice();
            say("    T1 first read : " + first);
            concurrentDepositOf50();
            BigDecimal second = readAlice();
            say("    T1 second read: " + second + "  -> same value: T1 works on the snapshot taken at its first query");
            return new BigDecimal[]{first, second};
        });
        say("    after both transactions, the database holds: " + readAlice());
        assertThat(reads[0]).isEqualByComparingTo("1000.00");
        assertThat(reads[1]).isEqualByComparingTo("1000.00");
        assertThat(readAlice()).isEqualByComparingTo("1050.00");
    }

    @Test @Order(3)
    void c3_repeatableRead_updatingARowChangedByOthersFails() {
        assertThatThrownBy(() -> tx(TransactionDefinition.ISOLATION_REPEATABLE_READ).executeWithoutResult(s -> {
            say("C3 REPEATABLE_READ, T1 reads, T2 commits a change, then T1 updates the same row");
            say("    T1 first read : " + readAlice());
            concurrentDepositOf50();
            jdbc.update("update accounts set balance = balance - 10.00 where owner_name = 'Alice'");
        })).isInstanceOf(ConcurrencyFailureException.class)
           .satisfies(e -> say("    T1 update failed: " + e.getClass().getSimpleName() + ": "
                   + ((ConcurrencyFailureException) e).getMostSpecificCause().getMessage().lines().findFirst().orElse("")));
        say("    the database holds: " + readAlice() + " (T2's change kept, T1 rolled back)");

        assertThat(readAlice()).isEqualByComparingTo("1050.00");
    }

    @Test @Order(4)
    void c4_readCommitted_updatingARowChangedByOthersSucceeds() {
        tx(TransactionDefinition.ISOLATION_READ_COMMITTED).executeWithoutResult(s -> {
            say("C4 READ_COMMITTED, the same scenario as C3");
            say("    T1 first read : " + readAlice());
            concurrentDepositOf50();
            jdbc.update("update accounts set balance = balance - 10.00 where owner_name = 'Alice'");
            say("    T1 update succeeded (the UPDATE re-reads the latest committed row)");
        });
        say("    the database holds: " + readAlice() + " (1000 + 50 - 10)");
        assertThat(readAlice()).isEqualByComparingTo("1040.00");
    }
}
