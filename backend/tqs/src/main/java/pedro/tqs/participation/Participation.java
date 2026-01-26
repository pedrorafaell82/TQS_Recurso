package pedro.tqs.participation;

import jakarta.persistence.*;
import pedro.tqs.opportunity.Opportunity;
import pedro.tqs.user.AppUser;

import java.time.Instant;

@Entity
@Table(
    uniqueConstraints = @UniqueConstraint(columnNames = {"volunteer_id", "opportunity_id"})
)
public class Participation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private AppUser volunteer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Opportunity opportunity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipationStatus status = ParticipationStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Participation() {}

    public Participation(AppUser volunteer, Opportunity opportunity) {
        this.volunteer = volunteer;
        this.opportunity = opportunity;
        this.status = ParticipationStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public AppUser getVolunteer() { return volunteer; }
    public Opportunity getOpportunity() { return opportunity; }
    public ParticipationStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    public void setStatus(ParticipationStatus status) { this.status = status; }
}
