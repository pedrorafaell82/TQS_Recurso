package pedro.tqs.reward.dto;

import java.time.Instant;

public record RewardRedemptionResponse(
        Long transactionId,
        Long rewardId,
        String rewardName,
        int cost,
        Instant createdAt
) {}
