package pedro.tqs.opportunity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Opportunity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String title;

    @Column(nullable=false, length=2000)
    private String description;

    @Column(nullable=false)
    private LocalDate date;

    @Column(nullable=false)
    private int durationHours;

    @Column(nullable=false)
    private int points;

    @Column(nullable=false)
    private boolean active = true;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private pedro.tqs.user.AppUser createdBy;

    protected Opportunity() {}

    public Opportunity(String title, String description, LocalDate date, int durationHours, int points, pedro.tqs.user.AppUser createdBy) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.durationHours = durationHours;
        this.points = points;
        this.createdBy = createdBy;
        this.active = true;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDate() { return date; }
    public int getDurationHours() { return durationHours; }
    public int getPoints() { return points; }
    public boolean isActive() { return active; }
    public pedro.tqs.user.AppUser getCreatedBy() { return createdBy; }
}
