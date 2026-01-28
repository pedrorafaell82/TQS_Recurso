package pedro.tqs.points;

import jakarta.persistence.*;
import pedro.tqs.participation.Participation;
import pedro.tqs.user.AppUser;
import pedro.tqs.reward.Reward;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participation_id", nullable = true)
    private Participation participation;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointTransactionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    private Reward reward;

    protected PointTransaction() {}

    public PointTransaction(AppUser user, Participation participation, int amount) {
        this.user = user;
        this.participation = participation;
        this.amount = amount;
        this.type = PointTransactionType.EARN;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public Participation getParticipation() { return participation; }
    public int getAmount() { return amount; }
    public Reward getReward() { return reward; }
    public Instant getCreatedAt() { return createdAt; }

    public static PointTransaction redeem(AppUser user, Reward reward, int cost) {
        PointTransaction tx = new PointTransaction();
        tx.user = user;
        tx.participation = null;
        tx.amount = -cost;
        tx.type = PointTransactionType.REDEEM;
        tx.reward = reward;
        tx.createdAt = Instant.now();
        return tx;
    }

}
