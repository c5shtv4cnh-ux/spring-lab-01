package kz.iitu.spring_lab_01;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Lab 10, task 2.2: prints BCrypt hashes for the two teaching passwords. */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        var encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("user123"));
        System.out.println(encoder.encode("admin123"));
    }
}
