package pedro.tqs.participation;

import org.springframework.data.jpa.repository.JpaRepository;
import pedro.tqs.opportunity.Opportunity;
import pedro.tqs.user.AppUser;

import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    Optional<Participation> findByVolunteerAndOpportunity(AppUser volunteer, Opportunity opportunity);
}
