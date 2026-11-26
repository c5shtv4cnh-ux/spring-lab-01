package kz.iitu.spring_lab_01.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import kz.iitu.spring_lab_01.notify.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Lab 13, task 2 (variant 3: welcome e-mail on signup).
 * The user is saved first (SignupService commits), then only the new id is handed to the async mailer.
 */
@RestController
@RequestMapping("/api/signup")
public class SignupController {

    private static final Logger log = LoggerFactory.getLogger(SignupController.class);

    /** The username is later written to the log: letters, digits, '.', '_' and '-' only (no CR/LF injection). */
    public record SignupRequest(@NotBlank @Pattern(regexp = "[A-Za-z0-9._-]{3,60}") String username,
                                @NotBlank @Size(min = 6, max = 72) String password) { }

    private final SignupService signups;
    private final NotificationService notifications;

    public SignupController(SignupService signups, NotificationService notifications) {
        this.signups = signups;
        this.notifications = notifications;
    }

    /** Correct: the call goes through the Spring proxy, the e-mail runs on an async- thread. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> signup(@Valid @RequestBody SignupRequest request) {
        long id = signups.register(request.username(), request.password());
        log.info("signup of user {} handled on {}", id, Thread.currentThread().getName());
        notifications.welcomeEmail(id);
        return Map.of("id", id, "username", request.username(), "thread", Thread.currentThread().getName());
    }

    /**
     * Broken on purpose (for the defence): NotificationService calls its own @Async method
     * through "this", the proxy is bypassed and the request waits the full 2 seconds.
     */
    @PostMapping("/self-invocation")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> signupSelfInvocation(@Valid @RequestBody SignupRequest request) {
        long id = signups.register(request.username(), request.password());
        log.info("signup of user {} handled on {} (self-invocation variant)", id, Thread.currentThread().getName());
        notifications.welcomeEmailViaSelfInvocation(id);
        return Map.of("id", id, "username", request.username(), "thread", Thread.currentThread().getName());
    }
}
