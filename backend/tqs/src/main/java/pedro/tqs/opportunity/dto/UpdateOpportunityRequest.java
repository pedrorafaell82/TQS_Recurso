package pedro.tqs.opportunity.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record UpdateOpportunityRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotNull LocalDate date,
        @Min(1) int durationHours,
        @Min(0) int points
) {}
