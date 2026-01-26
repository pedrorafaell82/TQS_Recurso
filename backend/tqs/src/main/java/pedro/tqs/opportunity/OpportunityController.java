package pedro.tqs.opportunity;

import pedro.tqs.opportunity.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
}
