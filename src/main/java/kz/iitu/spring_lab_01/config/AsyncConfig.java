package kz.iitu.spring_lab_01.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Lab 13, task 2: @Async methods run on Spring Boot's applicationTaskExecutor,
 * sized and named by spring.task.execution.* (prefix async-, core 4, max 8, queue 50).
 */
@Configuration
@EnableAsync
public class AsyncConfig { }
