package kz.iitu.spring_lab_01.web;

import kz.iitu.spring_lab_01.catalog.Book;
import kz.iitu.spring_lab_01.catalog.BookService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookRestController {

    private final BookService service;

    public BookRestController(BookService service) {
        this.service = service;
    }

    @GetMapping
    public List<Book> list(@RequestParam(required = false) String author,
                           @RequestParam(defaultValue = "10") int limit) {
        return service.findAll(author).stream().limit(limit).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> find(@PathVariable long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Book> create(@RequestBody Book book) {
        Book saved = service.create(book);
        return ResponseEntity
                .created(URI.create("/api/books/" + saved.id()))
                .body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> replace(@PathVariable long id, @RequestBody Book book) {
        return service.update(id, book)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // Individual variant 6
    @GetMapping(value = "/{id}/citation", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> citation(@PathVariable long id) {
        return service.findById(id)
                .map(b -> ResponseEntity.ok(
                        String.format("%s (%d). %s.", b.author(), b.year(), b.title())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}