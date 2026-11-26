package kz.iitu.spring_lab_01.user;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Lab 13, task 2: creates the user in its own transaction and returns only the identifier. */
@Service
public class SignupService {

    private final AppUserRepository users;
    private final PasswordEncoder encoder;

    public SignupService(AppUserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Transactional
    public long register(String username, String rawPassword) {
        if (users.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username '" + username + "' is taken");
        }
        return users.save(new AppUser(username, encoder.encode(rawPassword))).getId();
    }
}
