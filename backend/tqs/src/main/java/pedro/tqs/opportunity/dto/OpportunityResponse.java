package pedro.tqs.opportunity.dto;

import java.time.LocalDate;

public record OpportunityResponse(
        Long id,
        String title,
        String description,
        LocalDate date,
        int durationHours,
        int points
) {}
