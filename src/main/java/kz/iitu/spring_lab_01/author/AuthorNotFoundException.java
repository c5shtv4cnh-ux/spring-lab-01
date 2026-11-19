package kz.iitu.spring_lab_01.author;

public class AuthorNotFoundException extends RuntimeException {
    private final long id;

    public AuthorNotFoundException(long id) {
        super("Author " + id + " not found");
        this.id = id;
    }

    public long getId() { return id; }
}
