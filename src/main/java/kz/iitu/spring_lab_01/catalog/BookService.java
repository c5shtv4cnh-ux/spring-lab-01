package kz.iitu.spring_lab_01.catalog;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> findAll(String author) {
        List<Book> all = repository.findAll();
        if (author == null || author.isBlank()) {
            return all;
        }
        String needle = author.toLowerCase();
        return all.stream()
                .filter(b -> b.author().toLowerCase().contains(needle))
                .toList();
    }

    public Optional<Book> findById(long id) {
        return repository.findById(id);
    }

    public Book create(Book book) {
        return repository.save(new Book(null, book.title(), book.author(), book.year()));
    }

    public Optional<Book> update(long id, Book book) {
        if (repository.findById(id).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(repository.save(new Book(id, book.title(), book.author(), book.year())));
    }

    public boolean delete(long id) {
        return repository.deleteById(id);
    }
}