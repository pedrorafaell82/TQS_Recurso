package pedro.tqs.reward.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateRewardRequest(
        @NotBlank String name,
        @Min(1) int cost
) {}
