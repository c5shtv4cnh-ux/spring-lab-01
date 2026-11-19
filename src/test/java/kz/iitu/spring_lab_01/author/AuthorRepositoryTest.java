package kz.iitu.spring_lab_01.author;

import kz.iitu.spring_lab_01.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lab 12, task 3: data slice. Only JPA, Flyway and the repository are started; the database is a
 * PostgreSQL container (replace = NONE keeps it instead of an embedded database). Each test rolls back.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class AuthorRepositoryTest {

    @Autowired AuthorRepository repository;
    @Autowired TestEntityManager em;

    @Test
    void findsEveryAuthorWhoseNameContainsThePartIgnoringCaseSortedByName() {
        em.persist(new Author("Ursula K. Le Guin", "USA"));
        em.persist(new Author("ursula vernon", "USA"));
        em.flush();
        em.clear();          // force the query to hit the database

        List<Author> found = repository.findByNameContainingIgnoreCaseOrderByNameAsc("URSULA");

        assertThat(found).extracting(Author::getName)
                         .containsExactly("Ursula K. Le Guin", "ursula vernon");
    }

    @Test
    void excludesAuthorsWhoseNameDoesNotContainThePart() {
        em.persist(new Author("Isaac Asimov", "USA"));
        em.persist(new Author("Ursula K. Le Guin", "USA"));
        em.flush();
        em.clear();

        List<Author> found = repository.findByNameContainingIgnoreCaseOrderByNameAsc("guin");

        assertThat(found).extracting(Author::getName)
                         .containsExactly("Ursula K. Le Guin")
                         .doesNotContain("Isaac Asimov", "Frank Herbert");
    }
}
