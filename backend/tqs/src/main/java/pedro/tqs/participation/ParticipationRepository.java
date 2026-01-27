package pedro.tqs.participation;

import org.springframework.data.jpa.repository.JpaRepository;
import pedro.tqs.opportunity.Opportunity;
import pedro.tqs.user.AppUser;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    Optional<Participation> findByVolunteerAndOpportunity(AppUser volunteer, Opportunity opportunity);
    List<Participation> findByVolunteer_Email(String email);
    List<Participation> findByVolunteer_EmailOrderByCreatedAtDesc(String email);
}
