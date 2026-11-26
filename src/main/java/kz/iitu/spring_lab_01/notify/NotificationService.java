package kz.iitu.spring_lab_01.notify;

import kz.iitu.spring_lab_01.user.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Lab 13, task 2 (variant 3): the welcome e-mail after signup.
 * The method takes the user's identifier, not the AppUser entity: it runs on another thread,
 * outside the caller's transaction and persistence context, so it loads what it needs itself.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final AppUserRepository users;

    public NotificationService(AppUserRepository users) {
        this.users = users;
    }

    @Async
    public CompletableFuture<Void> welcomeEmail(long userId) {
        String username = users.findById(userId).map(u -> u.getUsername()).orElse("?");
        log.info("sending welcome e-mail to user {} ({}) on {}", userId, username, Thread.currentThread().getName());
        sleep(Duration.ofSeconds(2));                 // stands in for a mail server
        log.info("welcome e-mail to user {} sent on {}", userId, Thread.currentThread().getName());
        return CompletableFuture.completedFuture(null);
    }

    /** The classic mistake: a call through "this" never reaches the proxy, so @Async is ignored. */
    public CompletableFuture<Void> welcomeEmailViaSelfInvocation(long userId) {
        return welcomeEmail(userId);
    }

    private static void sleep(Duration d) {
        try {
            Thread.sleep(d);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
