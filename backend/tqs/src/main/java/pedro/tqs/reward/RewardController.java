package pedro.tqs.reward;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pedro.tqs.points.PointsService;
import pedro.tqs.reward.dto.RewardRedemptionResponse;
import pedro.tqs.reward.dto.RewardResponse;

import java.util.List;

@RestController
@RequestMapping("/api/rewards")
public class RewardController {

    private final RewardRepository rewards;
    private final PointsService points;

    public RewardController(RewardRepository rewards, PointsService points) {
        this.rewards = rewards;
        this.points = points;
    }

    @GetMapping
    public List<RewardResponse> listActive() {
        return rewards.findByActiveTrue().stream()
                .map(r -> new RewardResponse(r.getId(), r.getName(), r.getCost(), r.isActive()))
                .toList();
    }

    @PostMapping("/{id}/redeem")
    @PreAuthorize("hasRole('VOLUNTEER')")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void redeem(@PathVariable Long id, Authentication auth) {
        points.redeem(auth.getName(), id);
    }

    @GetMapping("/me/history")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public List<RewardRedemptionResponse> myHistory(Authentication auth) {
        return points.getMyRewardHistory(auth.getName());
    }
}
