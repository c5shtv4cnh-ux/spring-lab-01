package kz.iitu.spring_lab_01.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Lab 13, task 1: caching is switched on in its own configuration class, not on the application class.
 * The store is Caffeine (variant 3); its spec lives in application.properties (spring.cache.caffeine.spec).
 */
@Configuration
@EnableCaching
public class CacheConfig { }
