package kz.iitu.spring_lab_01.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/css/**", "/error").permitAll()
                .requestMatchers(HttpMethod.GET, "/books/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/books/**").hasRole("ADMIN")   // create, edit, delete
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().denyAll())
            .formLogin(form -> form.permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/?logout"));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
