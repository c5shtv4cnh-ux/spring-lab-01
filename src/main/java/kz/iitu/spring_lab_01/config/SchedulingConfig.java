package kz.iitu.spring_lab_01.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Lab 13, task 3: @Scheduled jobs run on the scheduler's threads, named by spring.task.scheduling.thread-name-prefix (sched-). */
@Configuration
@EnableScheduling
public class SchedulingConfig { }
