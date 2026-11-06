package kz.iitu.spring_lab_01.bank;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Task A — declarative transactions and rollback. */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TaskATransactionTests extends BankTestSupport {

    @Autowired TransferService service;

    @Test @Order(1)
    void a1_successfulTransfer() {
        show("A1 before transfer(Alice -> Bob, 100.00)");
        service.transfer(alice, bob, new BigDecimal("100.00"));
        show("A1 after  transfer(Alice -> Bob, 100.00)");

        assertThat(balance(alice)).isEqualByComparingTo("900.00");
        assertThat(balance(bob)).isEqualByComparingTo("600.00");
    }

    @Test @Order(2)
    void a2_insufficientFunds() {
        show("A2 before transfer(Alice -> Bob, 5000.00)");
        assertThatThrownBy(() -> service.transfer(alice, bob, new BigDecimal("5000.00")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Insufficient funds")
                .satisfies(e -> say("A2 exception: " + e));
        show("A2 after  failed transfer");

        assertThat(balance(alice)).isEqualByComparingTo("1000.00");
        assertThat(balance(bob)).isEqualByComparingTo("500.00");
    }

    @Test @Order(3)
    void a3_uncheckedExceptionAfterDebitRollsBack() {
        show("A3 before transferFailingAfterDebit(Alice -> Bob, 100.00)");
        assertThatThrownBy(() -> service.transferFailingAfterDebit(alice, bob, new BigDecimal("100.00")))
                .isInstanceOf(RuntimeException.class)
                .satisfies(e -> say("A3 exception: " + e));
        show("A3 after  - the debit was flushed (UPDATE executed), but the transaction rolled back");

        assertThat(balance(alice)).isEqualByComparingTo("1000.00");
        assertThat(balance(bob)).isEqualByComparingTo("500.00");
    }

    @Test @Order(4)
    void a4_checkedExceptionDoesNotRollBackByDefault() {
        show("A4 before transferFailingWithChecked(Alice -> Bob, 100.00)");
        assertThatThrownBy(() -> service.transferFailingWithChecked(alice, bob, new BigDecimal("100.00")))
                .isInstanceOf(TransferException.class)
                .satisfies(e -> say("A4 exception: " + e));
        show("A4 after  - checked exception: the transaction was COMMITTED, Alice lost 100, Bob got nothing");

        assertThat(balance(alice)).isEqualByComparingTo("900.00");
        assertThat(balance(bob)).isEqualByComparingTo("500.00");
    }

    @Test @Order(5)
    void a5_checkedExceptionWithRollbackFor() {
        show("A5 before transferFailingWithCheckedRollbackFor(Alice -> Bob, 100.00)");
        assertThatThrownBy(() -> service.transferFailingWithCheckedRollbackFor(alice, bob, new BigDecimal("100.00")))
                .isInstanceOf(TransferException.class)
                .satisfies(e -> say("A5 exception: " + e));
        show("A5 after  - rollbackFor = TransferException.class: rolled back");

        assertThat(balance(alice)).isEqualByComparingTo("1000.00");
        assertThat(balance(bob)).isEqualByComparingTo("500.00");
    }
}
