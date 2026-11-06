package kz.iitu.spring_lab_01.bank;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Task B: the inner method. A separate bean, so the call goes through the transactional proxy. */
@Service
public class AuditService {

    private final AuditRepository audit;

    public AuditService(AuditRepository audit) {
        this.audit = audit;
    }

    /** Joins the caller's transaction: commits or rolls back together with it. */
    @Transactional(propagation = Propagation.REQUIRED)
    public void logRequired(String message) {
        audit.save(new AuditRecord(message));
    }

    /** Suspends the caller's transaction and runs in its own, committed when this method returns. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logRequiresNew(String message) {
        audit.save(new AuditRecord(message));
    }
}
