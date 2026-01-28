package pedro.tqs.reward;

import jakarta.persistence.*;

@Entity
public class Reward {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private int cost;

    @Column(nullable = false)
    private boolean active = true;

    protected Reward() {}

    public Reward(String name, int cost) {
        this.name = name;
        this.cost = cost;
        this.active = true;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public int getCost() { return cost; }
    public boolean isActive() { return active; }

    public void setActive(boolean active) { this.active = active; }
    public void setCost(int cost) { this.cost = cost; }
    public void setName(String name) { this.name = name; }

}
