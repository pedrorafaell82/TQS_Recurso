package pedro.tqs.points;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pedro.tqs.participation.Participation;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import org.springframework.http.HttpStatus;
import pedro.tqs.reward.Reward;
import pedro.tqs.reward.RewardRepository;
import pedro.tqs.reward.dto.RewardRedemptionResponse;
import pedro.tqs.user.UserRepository;


@Service
public class PointsService {

    private final PointTransactionRepository repo;
    private final RewardRepository rewards;
    private final UserRepository users;

    public PointsService(PointTransactionRepository repo, RewardRepository rewards, UserRepository users) {
        this.repo = repo;
        this.rewards = rewards;
        this.users = users;
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

    @Transactional
    public void redeem(String email, Long rewardId) {
        var user = users.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Reward reward = rewards.findById(rewardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reward not found"));

        if (!reward.isActive()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reward is inactive");
        }

        int balance = repo.getBalanceForEmail(email.toLowerCase());
        if (balance < reward.getCost()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient points");
        }

        repo.save(PointTransaction.redeem(user, reward, reward.getCost()));
    }

    @Transactional(readOnly = true)
    public List<RewardRedemptionResponse> getMyRewardHistory(String email) {
        return repo.findRewardRedemptions(email).stream()
                .map(t -> new RewardRedemptionResponse(
                        t.getId(),
                        t.getReward().getId(),
                        t.getReward().getName(),
                        Math.abs(t.getAmount()),
                        t.getCreatedAt()
                ))
                .toList();
    }

}
