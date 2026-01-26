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
    private static final String USER_NOT_FOUND = "User not found";

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_NOT_FOUND));

        Opportunity opp = opportunities.findById(opportunityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opportunity not found"));

        if (!opp.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Opportunity is inactive");
        }

        participations.findByVolunteerAndOpportunity(volunteer, opp).ifPresent(existing -> {
            if (existing.getStatus() != ParticipationStatus.CANCELLED
                    && existing.getStatus() != ParticipationStatus.REJECTED) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Already enrolled");
            }
            existing.setStatus(ParticipationStatus.PENDING);
        });

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

    @Transactional
    public void approve(Long participationId, String promoterEmail) {
        Participation p = participations.findById(participationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participation not found"));

        AppUser promoter = users.findByEmail(promoterEmail.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_NOT_FOUND));

        if (!p.getOpportunity().getCreatedBy().getId().equals(promoter.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not owner of opportunity");
        }

        if (p.getStatus() != ParticipationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Participation not pending");
        }

        p.setStatus(ParticipationStatus.APPROVED);
    }

    @Transactional
    public void reject(Long participationId, String promoterEmail) {
        Participation p = participations.findById(participationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participation not found"));

        AppUser promoter = users.findByEmail(promoterEmail.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_NOT_FOUND));

        if (!p.getOpportunity().getCreatedBy().getId().equals(promoter.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not owner of opportunity");
        }

        if (p.getStatus() != ParticipationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Participation not pending");
        }

        p.setStatus(ParticipationStatus.REJECTED);
    }

    @Transactional(readOnly = true)
    public java.util.List<ParticipationResponse> getMyParticipations(String volunteerEmail) {
        users.findByEmail(volunteerEmail.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_NOT_FOUND));

        return participations.findByVolunteer_Email(volunteerEmail.toLowerCase())
                .stream()
                .map(this::toResponse)
                .toList();
    }


}
