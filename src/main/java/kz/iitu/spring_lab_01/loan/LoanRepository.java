package kz.iitu.spring_lab_01.loan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    /** One UPDATE for all loans that are past due, not returned and not yet marked. */
    @Modifying
    @Query("update Loan l set l.overdue = true where l.dueDate < :today and l.returned = false and l.overdue = false")
    int markOverdue(@Param("today") LocalDate today);

    long countByOverdueTrue();
}
