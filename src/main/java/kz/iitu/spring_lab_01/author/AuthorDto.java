package kz.iitu.spring_lab_01.author;

import java.io.Serializable;

/**
 * Lab 13: what the API returns and what the cache stores — never the JPA entity
 * (a cached entity is detached and would throw LazyInitializationException on a hit).
 * Serializable so that the same DTO would also work with a Redis cache.
 */
public record AuthorDto(Long id, String name, String country) implements Serializable {

    public static AuthorDto of(Author a) {
        return new AuthorDto(a.getId(), a.getName(), a.getCountry());
    }
}
