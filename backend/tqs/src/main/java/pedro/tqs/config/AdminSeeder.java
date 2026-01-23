package pedro.tqs.config;

import pedro.tqs.user.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class AdminSeeder {

    @Bean
    CommandLineRunner seedAdmin(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            String email = "admin@local.test";
            if (!users.existsByEmail(email)) {
                AppUser admin = new AppUser(
                        "Admin",
                        email,
                        encoder.encode("adminPass1"),
                        Set.of(Role.ADMIN)
                );
                users.save(admin);
            }
        };
    }
}
