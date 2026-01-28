package pedro.tqs.points.dto;

import jakarta.validation.constraints.DecimalMin;

public record UpdatePointRulesRequest(
        @DecimalMin("0.1") double approvalMultiplier
) {}
