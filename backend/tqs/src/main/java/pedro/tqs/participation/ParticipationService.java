package pedro.tqs.participation;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pedro.tqs.opportunity.Opportunity;
import pedro.tqs.opportunity.OpportunityRepository;
import pedro.tqs.participation.dto.ParticipationResponse;
import pedro.tqs.user.AppUser;
import pedro.tqs.user.UserRepository;

@Service
public class ParticipationService {

    private final ParticipationRepository participations;
    private final OpportunityRepository opportunities;
    private final UserRepository users;

    public ParticipationService(ParticipationRepository participations,
                                OpportunityRepository opportunities,
                                UserRepository users) {
        this.participations = participations;
        this.opportunities = opportunities;
        this.users = users;
    }

    @Transactional
    public ParticipationResponse enroll(Long opportunityId, String volunteerEmail) {
        AppUser volunteer = users.findByEmail(volunteerEmail.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Opportunity opp = opportunities.findById(opportunityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opportunity not found"));

        if (!opp.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Opportunity is inactive");
        }

        participations.findByVolunteerAndOpportunity(volunteer, opp).ifPresent(existing -> {
            // You can decide policy; this is simplest for MVP:
            if (existing.getStatus() != ParticipationStatus.CANCELLED
                    && existing.getStatus() != ParticipationStatus.REJECTED) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Already enrolled");
            }
            // if CANCELLED/REJECTED, we allow re-enroll by setting back to PENDING
            existing.setStatus(ParticipationStatus.PENDING);
            // returning from lambda isn't possible; so we throw to stop flow? better do below:
        });

        // Handle "re-enroll" cleanly:
        var existingOpt = participations.findByVolunteerAndOpportunity(volunteer, opp);
        if (existingOpt.isPresent()) {
            Participation existing = existingOpt.get();
            if (existing.getStatus() == ParticipationStatus.CANCELLED || existing.getStatus() == ParticipationStatus.REJECTED) {
                existing.setStatus(ParticipationStatus.PENDING);
                return toResponse(existing);
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already enrolled");
        }

        Participation saved = participations.save(new Participation(volunteer, opp));
        return toResponse(saved);
    }

    private ParticipationResponse toResponse(Participation p) {
        return new ParticipationResponse(
                p.getId(),
                p.getOpportunity().getId(),
                p.getStatus(),
                p.getCreatedAt()
        );
    }
}
