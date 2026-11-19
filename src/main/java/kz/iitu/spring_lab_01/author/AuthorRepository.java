package kz.iitu.spring_lab_01.author;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    /** Search used by GET /api/authors?q=... — case-insensitive, sorted by name. */
    List<Author> findByNameContainingIgnoreCaseOrderByNameAsc(String part);

    List<Author> findAllByOrderByNameAsc();
}
