package kz.iitu.spring_lab_01.loan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Lab 13, task 3 (variant 3): nightly overdue marking, every day at 02:00 Almaty time.
 * The zone is stated explicitly: without it the cron would follow the server's default zone
 * (UTC in a container), i.e. run at 07:00 in Almaty.
 * For the demonstration the expression can be overridden: --library.overdue.cron="0/20 * * * * *".
 */
@Component
public class OverdueMarkingJob {

    static final String ZONE = "Asia/Almaty";
    private static final Logger log = LoggerFactory.getLogger(OverdueMarkingJob.class);

    private final LoanRepository loans;

    public OverdueMarkingJob(LoanRepository loans) {
        this.loans = loans;
    }

    @Scheduled(cron = "${library.overdue.cron:0 0 2 * * *}", zone = ZONE)
    @Transactional
    public void markOverdueLoans() {
        LocalDate today = LocalDate.now(ZoneId.of(ZONE));
        int marked = loans.markOverdue(today);
        log.info("overdue marking for {}: {} loan(s) newly marked, {} overdue in total (thread {})",
                 today, marked, loans.countByOverdueTrue(), Thread.currentThread().getName());
    }
}
