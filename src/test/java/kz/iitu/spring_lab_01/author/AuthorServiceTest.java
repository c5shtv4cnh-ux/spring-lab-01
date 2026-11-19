package kz.iitu.spring_lab_01.author;

import kz.iitu.spring_lab_01.catalog.BookRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Lab 12, task 1 (variant 3: AuthorService). Pure unit tests: Mockito mocks, no Spring context.
 */
@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    @Mock AuthorRepository authors;
    @Mock BookRepository books;
    @InjectMocks AuthorService service;

    @Test
    void anExistingAuthorIsReturnedById() {
        given(authors.findById(4L)).willReturn(Optional.of(new Author(4L, "Frank Herbert", "USA")));

        Author found = service.byId(4L);

        assertThat(found.getName()).isEqualTo("Frank Herbert");
        assertThat(found.getCountry()).isEqualTo("USA");
    }

    @Test
    void aNewAuthorIsSavedWithoutSurroundingSpaces() {
        given(authors.save(any(Author.class))).willAnswer(inv -> inv.getArgument(0));

        service.create("  Ursula K. Le Guin  ", "  USA ");

        ArgumentCaptor<Author> saved = ArgumentCaptor.forClass(Author.class);
        verify(authors).save(saved.capture());
        assertThat(saved.getValue().getName()).isEqualTo("Ursula K. Le Guin");
        assertThat(saved.getValue().getCountry()).isEqualTo("USA");
    }

    /**
     * Task 5: JaCoCo showed `if (query == null || query.isBlank())` in search() as yellow — only the
     * "real query" way was taken (by the integration test). This test takes the other way.
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void aMissingOrBlankQueryListsEveryAuthorSortedByName(String query) {
        List<Author> all = List.of(new Author(2L, "Craig Walls", "USA"), new Author(4L, "Frank Herbert", "USA"));
        given(authors.findAllByOrderByNameAsc()).willReturn(all);

        assertThat(service.search(query)).isSameAs(all);
        verify(authors, never()).findByNameContainingIgnoreCaseOrderByNameAsc(any());
    }

    /** Variant 3: the branch that is easy to forget — an author with no books may be deleted. */
    @Test
    void anAuthorWithNoBooksIsDeleted() {
        Author herbert = new Author(4L, "Frank Herbert", "USA");
        given(authors.findById(4L)).willReturn(Optional.of(herbert));
        given(books.countByAuthor("Frank Herbert")).willReturn(0L);

        service.delete(4L);

        verify(authors).delete(herbert);
    }

    /** Variant 3, extra requirement: the failure cases are grouped in a @Nested class. */
    @Nested
    class WhenTheRequestCannotBeServed {

        @Test
        void anUnknownIdIsReportedAsAuthorNotFound() {
            given(authors.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.byId(99L))
                    .isInstanceOf(AuthorNotFoundException.class)
                    .hasMessage("Author 99 not found");
        }

        @Test
        void anAuthorWhoStillHasBooksIsNotDeleted() {
            Author bloch = new Author(1L, "Joshua Bloch", "USA");
            given(authors.findById(1L)).willReturn(Optional.of(bloch));
            given(books.countByAuthor("Joshua Bloch")).willReturn(1L);

            assertThatThrownBy(() -> service.delete(1L))
                    .isInstanceOf(AuthorHasBooksException.class)
                    .hasMessageContaining("still has 1 book(s)");
            verify(authors, never()).delete(any());
        }

        @Test
        void deletingAnUnknownAuthorDoesNotTouchTheCatalogue() {
            given(authors.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(AuthorNotFoundException.class);
            verify(books, never()).countByAuthor(any());
            verify(authors, never()).delete(any());
        }
    }
}
