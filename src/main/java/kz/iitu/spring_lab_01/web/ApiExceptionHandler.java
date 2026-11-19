package kz.iitu.spring_lab_01.web;

import kz.iitu.spring_lab_01.author.AuthorHasBooksException;
import kz.iitu.spring_lab_01.author.AuthorNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;

/**
 * Errors of the REST API as RFC 9457 problem details.
 * Extending ResponseEntityExceptionHandler also turns a failed @Valid body into a 400 problem detail.
 */
@RestControllerAdvice(basePackages = "kz.iitu.spring_lab_01.author")
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AuthorNotFoundException.class)
    ProblemDetail notFound(AuthorNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Author not found");
        pd.setType(URI.create("urn:problem:author-not-found"));
        pd.setProperty("authorId", ex.getId());
        return pd;
    }

    @ExceptionHandler(AuthorHasBooksException.class)
    ProblemDetail hasBooks(AuthorHasBooksException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Author still has books");
        return pd;
    }
}
