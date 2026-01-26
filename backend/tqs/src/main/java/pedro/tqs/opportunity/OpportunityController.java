package pedro.tqs.opportunity;

import pedro.tqs.opportunity.dto.*;
import pedro.tqs.user.AppUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/opportunities")
public class OpportunityController {

    private final OpportunityService service;

    public OpportunityController(OpportunityService service) {
        this.service = service;
    }

    @PreAuthorize("hasRole('PROMOTER')")
    @PostMapping
    public ResponseEntity<OpportunityResponse> create(@Valid @RequestBody CreateOpportunityRequest req,
                                                     Authentication auth) {
        OpportunityResponse created = service.create(req, auth.getName());
        return ResponseEntity.created(URI.create("/api/opportunities/" + created.id())).body(created);
    }

    @GetMapping
    public List<OpportunityResponse> listActive() {
        return service.listActive();
    }

    @GetMapping("/{id}")
    public OpportunityResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROMOTER')")
    public OpportunityResponse update(@PathVariable Long id,
                                    @Valid @RequestBody UpdateOpportunityRequest req,
                                    Authentication auth) {
        return service.update(id, req, auth.getName());
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('PROMOTER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id, Authentication auth) {
        service.deactivate(id, auth.getName());
    }

}
