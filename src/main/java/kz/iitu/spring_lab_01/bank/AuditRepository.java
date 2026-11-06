package kz.iitu.spring_lab_01.bank;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<AuditRecord, Long> {

    boolean existsByMessage(String message);
}
