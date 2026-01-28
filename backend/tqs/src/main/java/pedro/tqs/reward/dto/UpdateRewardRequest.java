package pedro.tqs.reward.dto;

import jakarta.validation.constraints.Min;

public record UpdateRewardRequest(
        String name,
        @Min(1) Integer cost,
        Boolean active
) {}
