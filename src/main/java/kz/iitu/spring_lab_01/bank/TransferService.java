package kz.iitu.spring_lab_01.bank;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransferService {

    private final AccountRepository repository;
    private final AuditService auditService;

    public TransferService(AccountRepository repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    // ---------------- Task A ----------------

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        Account from = repository.findById(fromId).orElseThrow();
        Account to = repository.findById(toId).orElseThrow();

        from.debit(amount);
        to.credit(amount);
        repository.save(from);
        repository.save(to);
    }

    /** Same as transfer, but fails with an unchecked exception after the debit and before the credit. */
    @Transactional
    public void transferFailingAfterDebit(Long fromId, Long toId, BigDecimal amount) {
        Account from = repository.findById(fromId).orElseThrow();
        Account to = repository.findById(toId).orElseThrow();

        from.debit(amount);
        repository.saveAndFlush(from);          // the UPDATE really reaches the database
        if (true) {
            throw new RuntimeException("Simulated failure after debit, before credit");
        }
        to.credit(amount);
        repository.save(to);
    }

    /** A checked exception: by default Spring does NOT roll back, the debit is committed. */
    @Transactional
    public void transferFailingWithChecked(Long fromId, Long toId, BigDecimal amount) throws TransferException {
        Account from = repository.findById(fromId).orElseThrow();
        from.debit(amount);
        repository.save(from);
        throw new TransferException("Checked failure after debit, before credit");
    }

    /** The same checked exception, but rollbackFor makes Spring roll back. */
    @Transactional(rollbackFor = TransferException.class)
    public void transferFailingWithCheckedRollbackFor(Long fromId, Long toId, BigDecimal amount) throws TransferException {
        Account from = repository.findById(fromId).orElseThrow();
        from.debit(amount);
        repository.save(from);
        throw new TransferException("Checked failure after debit, before credit (rollbackFor)");
    }

    // ---------------- Task B ----------------

    /** Outer transaction; the audit joins it (REQUIRED); then the outer method fails. */
    @Transactional
    public void transferWithAuditRequiredThenFail(Long fromId, Long toId, BigDecimal amount, String auditMessage) {
        transferInternal(fromId, toId, amount);
        auditService.logRequired(auditMessage);
        throw new IllegalStateException("Outer transaction fails after the audit returned");
    }

    /** Outer transaction; the audit runs in its own transaction (REQUIRES_NEW); then the outer method fails. */
    @Transactional
    public void transferWithAuditRequiresNewThenFail(Long fromId, Long toId, BigDecimal amount, String auditMessage) {
        transferInternal(fromId, toId, amount);
        auditService.logRequiresNew(auditMessage);
        throw new IllegalStateException("Outer transaction fails after the audit returned");
    }

    private void transferInternal(Long fromId, Long toId, BigDecimal amount) {
        Account from = repository.findById(fromId).orElseThrow();
        Account to = repository.findById(toId).orElseThrow();
        from.debit(amount);
        to.credit(amount);
    }
}
