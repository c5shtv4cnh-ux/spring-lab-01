package kz.iitu.spring_lab_01.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    /**
     * Lab 13, task 4: /api/slow is permitted only when the "perf" profile sets lab13.permit-slow=true.
     * In the default configuration (the one that gets merged) the flag is false and the path stays denied.
     */
    @Value("${lab13.permit-slow:false}")
    private boolean permitSlow;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, CustomAccessDeniedHandler accessDeniedHandler) throws Exception {
        http
            .authorizeHttpRequests(auth -> {
                if (permitSlow) {
                    auth.requestMatchers(HttpMethod.GET, "/api/slow").permitAll();     // perf profile only
                }
                auth
                    .requestMatchers("/", "/login", "/css/**", "/error",
                                     CustomAccessDeniedHandler.DENIED_PATH).permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/signup", "/api/signup/**").permitAll()   // lab 13
                    .requestMatchers("/actuator/health").permitAll()                             // lab 13
                    .requestMatchers("/actuator/**").hasRole("ADMIN")                            // /actuator/caches
                    .requestMatchers(HttpMethod.GET, "/books/**").hasAnyRole("USER", "ADMIN")
                    .requestMatchers("/books/**").hasRole("ADMIN")   // create, edit, delete
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/authors/**").hasAnyRole("USER", "ADMIN")   // lab 12
                    .requestMatchers("/api/authors/**").hasRole("ADMIN")                     // POST, PUT, DELETE
                    .anyRequest().denyAll();
            })
            .formLogin(form -> form
                .loginPage("/login")              // our own login template (templates/login.html)
                .defaultSuccessUrl("/books")
                .permitAll())
            .httpBasic(Customizer.withDefaults())  // lab 13: lets curl call the API and the actuator (-u admin:...)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/signup", "/api/signup/**"))   // anonymous, no session to forge
            .logout(logout -> logout.logoutSuccessUrl("/login?logout"))
            .exceptionHandling(ex -> ex
                // without credentials the user is still sent to /login (lab 10); httpBasic only
                // authenticates requests that already carry an Authorization header
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                .accessDeniedHandler(accessDeniedHandler));   // variant 3: our own 403 page
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
