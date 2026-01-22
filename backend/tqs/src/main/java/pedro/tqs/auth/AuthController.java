package pedro.tqs.auth;

import pedro.tqs.auth.dto.RegisterRequest;
import pedro.tqs.auth.dto.UserResponse;
import pedro.tqs.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse created = userService.register(request);
        return ResponseEntity
                .created(URI.create("/api/users/" + created.id()))
                .body(created);
    }
}
