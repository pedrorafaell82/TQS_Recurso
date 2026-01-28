package pedro.tqs.reward;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pedro.tqs.reward.dto.CreateRewardRequest;
import pedro.tqs.reward.dto.RewardResponse;
import pedro.tqs.reward.dto.UpdateRewardRequest;

import java.util.List;

@RestController
@RequestMapping("/api/admin/rewards")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRewardController {

    private final RewardRepository rewards;

    public AdminRewardController(RewardRepository rewards) {
        this.rewards = rewards;
    }

    @GetMapping
    public List<RewardResponse> listAll() {
        return rewards.findAll().stream()
                .map(r -> new RewardResponse(r.getId(), r.getName(), r.getCost(), r.isActive()))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RewardResponse create(@Valid @RequestBody CreateRewardRequest req) {
        Reward r = rewards.save(new Reward(req.name(), req.cost()));
        return new RewardResponse(r.getId(), r.getName(), r.getCost(), r.isActive());
    }

    @PatchMapping("/{id}")
    public RewardResponse update(@PathVariable Long id, @Valid @RequestBody UpdateRewardRequest req) {
        Reward r = rewards.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reward not found"));

        if (req.name() != null && !req.name().isBlank()) {
            r.setName(req.name());
        }
        if (req.cost() != null) {
            r.setCost(req.cost());
        }
        if (req.active() != null) {
            r.setActive(req.active());
        }

        Reward saved = rewards.save(r);
        return new RewardResponse(saved.getId(), saved.getName(), saved.getCost(), saved.isActive());
    }
}
