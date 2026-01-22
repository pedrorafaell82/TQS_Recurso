package pedro.tqs.user;

import pedro.tqs.auth.dto.RegisterRequest;
import pedro.tqs.auth.dto.UserResponse;
import pedro.tqs.user.exception.EmailAlreadyExistsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        String hash = passwordEncoder.encode(request.password());
        AppUser user = new AppUser(
                request.name().trim(),
                normalizedEmail,
                hash,
                Set.of(Role.VOLUNTEER)
        );

        AppUser saved = userRepository.save(user);

        return new UserResponse(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getRoles().stream().map(Enum::name).collect(Collectors.toSet())
        );
    }

    @Transactional(readOnly = true)
    public AppUser loadByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
