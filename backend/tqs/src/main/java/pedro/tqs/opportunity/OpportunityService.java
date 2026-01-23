package pedro.tqs.opportunity;

import pedro.tqs.opportunity.dto.*;
import pedro.tqs.user.AppUser;
import pedro.tqs.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OpportunityService {

    private final OpportunityRepository repo;
    private final UserRepository users;

    public OpportunityService(OpportunityRepository repo, UserRepository users) {
        this.repo = repo;
        this.users = users;
    }

    @Transactional
    public OpportunityResponse create(CreateOpportunityRequest req, String promoterEmail) {
        AppUser promoter = users.findByEmail(promoterEmail.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Opportunity saved = repo.save(new Opportunity(
                req.title().trim(),
                req.description().trim(),
                req.date(),
                req.durationHours(),
                req.points(),
                promoter
        ));

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OpportunityResponse> listActive() {
        return repo.findByActiveTrue().stream().map(this::toResponse).toList();
    }

    private OpportunityResponse toResponse(Opportunity o) {
        return new OpportunityResponse(
                o.getId(),
                o.getTitle(),
                o.getDescription(),
                o.getDate(),
                o.getDurationHours(),
                o.getPoints()
        );
    }
}
