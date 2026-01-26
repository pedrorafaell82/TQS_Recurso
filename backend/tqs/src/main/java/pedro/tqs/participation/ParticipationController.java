package pedro.tqs.participation;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/participations")
public class ParticipationController {

    private final ParticipationService service;

    public ParticipationController(ParticipationService service) {
        this.service = service;
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('PROMOTER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void approve(@PathVariable Long id, Authentication auth) {
        service.approve(id, auth.getName());
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('PROMOTER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reject(@PathVariable Long id, Authentication auth) {
        service.reject(id, auth.getName());
    }
}
