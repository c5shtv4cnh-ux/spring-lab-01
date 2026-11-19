package kz.iitu.spring_lab_01.author;

public class AuthorHasBooksException extends RuntimeException {
    public AuthorHasBooksException(String name, long books) {
        super("Author '" + name + "' still has " + books + " book(s) in the catalogue and cannot be deleted");
    }
}
