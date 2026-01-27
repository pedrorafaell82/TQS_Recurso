package pedro.tqs.participation;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import pedro.tqs.participation.dto.ParticipationResponse;

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

    @GetMapping("/me")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public List<ParticipationResponse> myParticipations(Authentication auth) {
        return service.getMyParticipations(auth.getName());
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('VOLUNTEER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id, Authentication auth) {
        service.cancel(id, auth.getName());
    }

    @GetMapping("/me/history")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public List<ParticipationResponse> myHistory(Authentication auth) {
        return service.getMyParticipationHistory(auth.getName());
    }

}
