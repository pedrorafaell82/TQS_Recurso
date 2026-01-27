package pedro.tqs.points;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pedro.tqs.participation.Participation;

@Service
public class PointsService {

    private final PointTransactionRepository repo;

    public PointsService(PointTransactionRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void awardForApprovedParticipation(Participation participation) {
        Long participationId = participation.getId();

        // Idempotency: award only once per participation
        if (repo.existsByParticipation_Id(participationId)) {
            return;
        }

        int points = participation.getOpportunity().getPoints();
        repo.save(new PointTransaction(participation.getVolunteer(), participation, points));
    }

    @Transactional(readOnly = true)
    public int getBalanceForUser(String email) {
        return repo.getBalanceForEmail(email.toLowerCase());
    }

}
