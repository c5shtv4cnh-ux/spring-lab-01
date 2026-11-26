package kz.iitu.spring_lab_01.author;

import kz.iitu.spring_lab_01.catalog.BookRepository;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lab 13, task 1 (variant 3): AuthorService.byId is cached in Caffeine, cache "authors".
 * Every write path that changes or removes an author evicts its entry.
 */
@Service
@CacheConfig(cacheNames = "authors")
public class AuthorService {

    private final AuthorRepository authors;
    private final BookRepository books;

    public AuthorService(AuthorRepository authors, BookRepository books) {
        this.authors = authors;
        this.books = books;
    }

    /** First call: one SELECT; later calls with the same id are answered by the cache (no SQL). */
    @Cacheable(key = "#id", unless = "#result == null")
    @Transactional(readOnly = true)
    public AuthorDto byId(long id) {
        return AuthorDto.of(find(id));
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
        return authors.save(new Author(name.strip(), clean(country)));
    }

    /** Lab 13: a write path — the cached copy of this author is evicted. */
    @CacheEvict(key = "#id")
    @Transactional
    public AuthorDto update(long id, String name, String country) {
        Author author = find(id);
        author.change(name.strip(), clean(country));
        return AuthorDto.of(authors.saveAndFlush(author));
    }

    /** An author may be deleted only when no book in the catalogue refers to him or her. Evicts too. */
    @CacheEvict(key = "#id")
    @Transactional
    public void delete(long id) {
        Author author = find(id);
        long bookCount = books.countByAuthor(author.getName());
        if (bookCount > 0) {
            throw new AuthorHasBooksException(author.getName(), bookCount);
        }
        authors.delete(author);
    }

    private Author find(long id) {
        return authors.findById(id).orElseThrow(() -> new AuthorNotFoundException(id));
    }

    private static String clean(String country) {
        return country == null || country.isBlank() ? null : country.strip();
    }
}
