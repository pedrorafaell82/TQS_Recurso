package pedro.tqs.user;

import pedro.tqs.user.dto.UpdateRolesRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/roles")
    public ResponseEntity<Void> updateRoles(@PathVariable Long id, @Valid @RequestBody UpdateRolesRequest req) {
        userService.updateRoles(id, req.roles());
        return ResponseEntity.noContent().build();
    }
}
