package pedro.tqs.auth;

import pedro.tqs.auth.dto.UserResponse;
import pedro.tqs.user.AppUser;
import pedro.tqs.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class CurrentUserController {

    private final UserService userService;

    public CurrentUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        // With HTTP Basic, username = email
        String email = authentication.getName();
        AppUser user = userService.loadByEmail(email);

        Set<String> roles = user.getRoles().stream().map(Enum::name).collect(Collectors.toSet());
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), roles);
    }
}