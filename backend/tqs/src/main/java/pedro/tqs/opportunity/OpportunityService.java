package pedro.tqs.opportunity;

import pedro.tqs.opportunity.dto.*;
import pedro.tqs.user.AppUser;
import pedro.tqs.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class OpportunityService {

    private final OpportunityRepository repo;
    private final UserRepository users;
    private static final String USER_NOT_FOUND = "User not found";
    private static final String OPPORTUNITY_NOT_FOUND = "Opportunity not found";

    public OpportunityService(OpportunityRepository repo, UserRepository users) {
        this.repo = repo;
        this.users = users;
    }

    @Transactional
    public OpportunityResponse create(CreateOpportunityRequest req, String promoterEmail) {
        AppUser promoter = users.findByEmail(promoterEmail.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_NOT_FOUND));

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
    public OpportunityResponse getById(Long id) {
        Opportunity o = repo.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        OPPORTUNITY_NOT_FOUND
                ));
        return toResponse(o);
    }

    @Transactional
    public OpportunityResponse update(Long id, UpdateOpportunityRequest req, String currentUserEmail) {
        Opportunity o = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, OPPORTUNITY_NOT_FOUND));

        AppUser currentUser = users.findByEmail(currentUserEmail.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_NOT_FOUND));

        if (!o.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Opportunity is inactive");
        }

        if (!o.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not owner");
        }

        o.setTitle(req.title().trim());
        o.setDescription(req.description().trim());
        o.setDate(req.date());
        o.setDurationHours(req.durationHours());
        o.setPoints(req.points());

        return toResponse(o);
    }

    @Transactional
    public void deactivate(Long id, String currentUserEmail) {
        Opportunity o = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, OPPORTUNITY_NOT_FOUND));

        AppUser currentUser = users.findByEmail(currentUserEmail.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_NOT_FOUND));

        if (!o.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not owner");
        }

        o.setActive(false);
    }

    @Transactional(readOnly = true)
    public List<OpportunityResponse> search(String q,
                                        Integer minPoints,
                                        Integer maxPoints,
                                        java.time.LocalDate dateFrom,
                                        java.time.LocalDate dateTo,
                                        Boolean active) {

        // comportamento atual: por defeito só ativas
        Boolean effectiveActive = (active == null) ? Boolean.TRUE : active;

        var spec = org.springframework.data.jpa.domain.Specification
                .where(OpportunitySpecifications.isActive(effectiveActive))
                .and(OpportunitySpecifications.queryText(q))
                .and(OpportunitySpecifications.minPoints(minPoints))
                .and(OpportunitySpecifications.maxPoints(maxPoints))
                .and(OpportunitySpecifications.dateFrom(dateFrom))
                .and(OpportunitySpecifications.dateTo(dateTo));

        return repo.findAll(spec).stream().map(this::toResponse).toList();
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
