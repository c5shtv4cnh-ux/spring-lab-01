package kz.iitu.spring_lab_01.author;

import kz.iitu.spring_lab_01.catalog.BookRepository;
import kz.iitu.spring_lab_01.config.CacheConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Lab 13, task 1: the caching proxy around the real AuthorService, a Caffeine cache manager
 * and a mocked repository — the number of findById calls is the number of SELECTs.
 */
@SpringJUnitConfig
class AuthorServiceCacheTest {

    @Configuration
    @Import({CacheConfig.class, AuthorService.class})
    static class Config {
        @Bean
        CacheManager cacheManager() {
            return new CaffeineCacheManager("authors");
        }
    }

    @Autowired AuthorService service;
    @Autowired CacheManager caches;
    @MockitoBean AuthorRepository authors;
    @MockitoBean BookRepository books;

    @BeforeEach
    void emptyCache() {
        caches.getCache("authors").clear();
        clearInvocations(authors);
    }

    @Test
    void theSecondIdenticalReadIsServedByTheCache() {
        given(authors.findById(4L)).willReturn(Optional.of(new Author(4L, "Frank Herbert", "USA")));

        AuthorDto first = service.byId(4L);
        AuthorDto second = service.byId(4L);

        assertThat(second).isEqualTo(first);
        verify(authors, times(1)).findById(4L);
        assertThat(caches.getCache("authors").get(4L, AuthorDto.class)).isEqualTo(first);
    }

    @Test
    void anUpdateEvictsSoTheNextReadGoesToTheDatabaseAgain() {
        given(authors.findById(4L)).willReturn(Optional.of(new Author(4L, "Frank Herbert", "USA")));
        given(authors.saveAndFlush(any(Author.class))).willAnswer(inv -> inv.getArgument(0));

        service.byId(4L);
        service.update(4L, "Frank Patrick Herbert", "USA");
        assertThat(caches.getCache("authors").get(4L)).isNull();

        AuthorDto after = service.byId(4L);
        assertThat(after.name()).isEqualTo("Frank Patrick Herbert");
        verify(authors, times(3)).findById(4L);   // read, update's own read, read after eviction
    }

    @Test
    void aDeleteEvictsToo() {
        given(authors.findById(4L)).willReturn(Optional.of(new Author(4L, "Frank Herbert", "USA")));

        service.byId(4L);
        service.delete(4L);

        assertThat(caches.getCache("authors").get(4L)).isNull();
    }
}
