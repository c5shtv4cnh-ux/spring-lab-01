package kz.iitu.spring_lab_01.author;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    public record AuthorRequest(@NotBlank @Size(max = 120) String name,
                                @Size(max = 60) String country) { }


    private final AuthorService service;

    public AuthorController(AuthorService service) {
        this.service = service;
    }

    @GetMapping
    public List<AuthorDto> search(@RequestParam(required = false) String q) {
        return service.search(q).stream().map(AuthorDto::of).toList();
    }

    @GetMapping("/{id}")
    public AuthorDto byId(@PathVariable long id) {
        return service.byId(id);
    }

    /** Lab 13: update — evicts the cached author. */
    @PutMapping("/{id}")
    public AuthorDto update(@PathVariable long id, @Valid @RequestBody AuthorRequest request) {
        return service.update(id, request.name(), request.country());
    }

    @PostMapping
    public ResponseEntity<AuthorDto> create(@Valid @RequestBody AuthorRequest request) {
        Author saved = service.create(request.name(), request.country());
        return ResponseEntity.created(URI.create("/api/authors/" + saved.getId())).body(AuthorDto.of(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
