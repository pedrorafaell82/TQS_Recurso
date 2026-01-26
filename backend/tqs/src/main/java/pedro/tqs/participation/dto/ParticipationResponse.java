package pedro.tqs.participation.dto;

import pedro.tqs.participation.ParticipationStatus;

import java.time.Instant;

public record ParticipationResponse(
        Long id,
        Long opportunityId,
        ParticipationStatus status,
        Instant createdAt
) {}
