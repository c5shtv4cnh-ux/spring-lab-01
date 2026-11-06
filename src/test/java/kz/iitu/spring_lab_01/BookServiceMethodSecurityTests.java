package kz.iitu.spring_lab_01;

import kz.iitu.spring_lab_01.catalog.Book;
import kz.iitu.spring_lab_01.catalog.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Lab 10, task 3.2: the service is protected even when it is called without
 * going through a URL (another controller, a scheduled job, a message listener).
 */
@SpringBootTest
class BookServiceMethodSecurityTests {

    @Autowired
    BookService books;

    @Test
    void anonymousCallerCannotDelete() {
        assertThatThrownBy(() -> books.delete(1))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void userCannotDeleteOrCreate() {
        assertThatThrownBy(() -> books.delete(1)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> books.create(new Book(null, "T", "A", 2020)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    void adminCanCreateAndDelete() {
        Book saved = books.create(new Book(null, "Temp", "Admin", 2026));
        assertThat(books.delete(saved.id())).isTrue();
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void readingIsNotRestricted() {
        assertThat(books.findAll(null)).isNotEmpty();
    }
}
