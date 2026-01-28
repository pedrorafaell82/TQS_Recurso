package pedro.tqs.points;

import jakarta.persistence.*;

@Entity
public class PointRuleConfig {

    @Id
    private Long id = 1L;

    @Column(nullable = false)
    private double approvalMultiplier = 1.0;

    protected PointRuleConfig() {}

    public PointRuleConfig(double approvalMultiplier) {
        this.id = 1L;
        this.approvalMultiplier = approvalMultiplier;
    }

    public Long getId() { return id; }
    public double getApprovalMultiplier() { return approvalMultiplier; }
    public void setApprovalMultiplier(double approvalMultiplier) { this.approvalMultiplier = approvalMultiplier; }
}
