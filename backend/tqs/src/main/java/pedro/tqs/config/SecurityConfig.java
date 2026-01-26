package pedro.tqs.config;

import pedro.tqs.user.UserRepository;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByEmail(username.toLowerCase())
                .map(u -> User.withUsername(u.getEmail())
                        .password(u.getPasswordHash())
                        .authorities(u.getRoles().stream().map(r -> "ROLE_" + r.name()).toArray(String[]::new))
                        .build()
                )
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Stateless REST API: no HTTP session, no cookies
            .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS))
            /*
            * CSRF protection is only relevant for browser-based sessions using cookies.
            * This project uses stateless HTTP Basic for /api/** (no cookies), so CSRF is not required there.
            * We keep CSRF enabled by default and ignore it only for API endpoints and the H2 console.
            */
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/h2-console/**"))

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .httpBasic(basic -> {});

        return http.build();
    }
}
