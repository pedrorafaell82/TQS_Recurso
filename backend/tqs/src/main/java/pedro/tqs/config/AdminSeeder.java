package pedro.tqs.config;

import pedro.tqs.user.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class AdminSeeder {

    @Bean
    CommandLineRunner seedAdmin(
            UserRepository users,
            PasswordEncoder encoder,
            @Value("${app.admin.email:admin@local.test}") String adminEmail,
            @Value("${app.admin.password:}") String adminPassword
    ) {
        return args -> {
            if (adminPassword == null || adminPassword.isBlank()) {
                return;
            }

            if (!users.existsByEmail(adminEmail)) {
                AppUser admin = new AppUser(
                        "Admin",
                        adminEmail,
                        encoder.encode(adminPassword),
                        Set.of(pedro.tqs.user.Role.ADMIN)
                );
                users.save(admin);
            }
        }; 
    }
}
