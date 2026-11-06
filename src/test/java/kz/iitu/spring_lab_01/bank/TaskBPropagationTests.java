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

/** Task B — propagation: REQUIRED vs REQUIRES_NEW for the inner audit method. */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TaskBPropagationTests extends BankTestSupport {

    @Autowired TransferService service;

    @Test @Order(1)
    void b1_required_auditIsRolledBackWithTheOuterTransaction() {
        show("B1 before outer transfer + audit(REQUIRED) + failure");
        assertThatThrownBy(() -> service.transferWithAuditRequiredThenFail(
                alice, bob, new BigDecimal("100.00"), "B1 transfer 100 (REQUIRED)"))
                .isInstanceOf(IllegalStateException.class)
                .satisfies(e -> say("B1 exception: " + e));
        show("B1 after");
        say("B1 audit record 'B1 transfer 100 (REQUIRED)' exists: " + audit.existsByMessage("B1 transfer 100 (REQUIRED)"));

        assertThat(audit.existsByMessage("B1 transfer 100 (REQUIRED)")).isFalse();
        assertThat(balance(alice)).isEqualByComparingTo("1000.00");
    }

    @Test @Order(2)
    void b2_requiresNew_auditSurvivesTheOuterRollback() {
        show("B2 before outer transfer + audit(REQUIRES_NEW) + failure");
        assertThatThrownBy(() -> service.transferWithAuditRequiresNewThenFail(
                alice, bob, new BigDecimal("100.00"), "B2 transfer 100 (REQUIRES_NEW)"))
                .isInstanceOf(IllegalStateException.class)
                .satisfies(e -> say("B2 exception: " + e));
        show("B2 after");
        say("B2 audit record 'B2 transfer 100 (REQUIRES_NEW)' exists: " + audit.existsByMessage("B2 transfer 100 (REQUIRES_NEW)"));

        assertThat(audit.existsByMessage("B2 transfer 100 (REQUIRES_NEW)")).isTrue();
        assertThat(balance(alice)).isEqualByComparingTo("1000.00");
        assertThat(balance(bob)).isEqualByComparingTo("500.00");
    }
}
