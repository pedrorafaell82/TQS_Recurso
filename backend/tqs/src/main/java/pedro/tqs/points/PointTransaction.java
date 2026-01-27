package pedro.tqs.points;

import jakarta.persistence.*;
import pedro.tqs.participation.Participation;
import pedro.tqs.user.AppUser;

import java.time.Instant;

@Entity
@Table(name = "point_transactions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"participation_id"}))
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private AppUser user;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private Participation participation;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected PointTransaction() {}

    public PointTransaction(AppUser user, Participation participation, int amount) {
        this.user = user;
        this.participation = participation;
        this.amount = amount;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public Participation getParticipation() { return participation; }
    public int getAmount() { return amount; }
    public Instant getCreatedAt() { return createdAt; }
}
