package kz.iitu.spring_lab_01.author;

import kz.iitu.spring_lab_01.catalog.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthorService {

    private final AuthorRepository authors;
    private final BookRepository books;

    public AuthorService(AuthorRepository authors, BookRepository books) {
        this.authors = authors;
        this.books = books;
    }

    @Transactional(readOnly = true)
    public Author byId(long id) {
        return authors.findById(id).orElseThrow(() -> new AuthorNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Author> search(String query) {
        if (query == null || query.isBlank()) {
            return authors.findAllByOrderByNameAsc();
        }
        return authors.findByNameContainingIgnoreCaseOrderByNameAsc(query.strip());
    }

    @Transactional
    public Author create(String name, String country) {
        String cleanCountry = country == null || country.isBlank() ? null : country.strip();
        return authors.save(new Author(name.strip(), cleanCountry));
    }

    /** An author may be deleted only when no book in the catalogue refers to him or her. */
    @Transactional
    public void delete(long id) {
        Author author = byId(id);
        long bookCount = books.countByAuthor(author.getName());
        if (bookCount > 0) {
            throw new AuthorHasBooksException(author.getName(), bookCount);
        }
        authors.delete(author);
    }
}
