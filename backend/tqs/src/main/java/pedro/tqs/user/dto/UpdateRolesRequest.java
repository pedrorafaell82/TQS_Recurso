package pedro.tqs.user.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record UpdateRolesRequest(
        @NotEmpty(message = "roles must not be empty")
        Set<String> roles
) {}
