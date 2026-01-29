package pedro.tqs.seed;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import pedro.tqs.user.AppUser;
import pedro.tqs.user.Role;
import pedro.tqs.user.UserRepository;
import pedro.tqs.reward.Reward;
import pedro.tqs.reward.RewardRepository;
import pedro.tqs.opportunity.Opportunity;
import pedro.tqs.opportunity.OpportunityRepository;
import pedro.tqs.points.PointRuleConfig;
import pedro.tqs.points.PointRuleConfigRepository;

import java.time.LocalDate;
import java.util.Set;



@Profile("dev")
@Component
public class DevSeeder implements CommandLineRunner {

    private final UserRepository users;
    private final RewardRepository rewards;
    private final OpportunityRepository opps;
    private final PointRuleConfigRepository rules;
    private final PasswordEncoder encoder;

        private static final String ADMIN_EMAIL = "admin@local.test";
        private static final String PROMOTER_EMAIL = "promoter@test.com";
        private static final String VOL_EMAIL = "vol@test.com";

        private static final String ENV_ADMIN_PASS = "DEV_ADMIN_PASSWORD";
        private static final String ENV_USER_PASS = "DEV_USER_PASSWORD";

        private String devAdminPassword() {
        String p = System.getenv(ENV_ADMIN_PASS);
        return (p != null && !p.isBlank()) ? p : "change-me-admin";
        }

        private String devUserPassword() {
        String p = System.getenv(ENV_USER_PASS);
        return (p != null && !p.isBlank()) ? p : "change-me-user";
        }

    public DevSeeder(UserRepository users,
                     RewardRepository rewards,
                     OpportunityRepository opps,
                     PointRuleConfigRepository rules,
                     PasswordEncoder encoder) {
        this.users = users;
        this.rewards = rewards;
        this.opps = opps;
        this.rules = rules;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        seedRules();
        seedUsers();
        seedRewards();
        seedOpportunities();
    }

    private void seedRules() {
        if (rules.count() == 0) {
            rules.save(new PointRuleConfig(1.0));
        }
    }

    private void seedUsers() {
        // ADMIN
        if (users.findByEmail(ADMIN_EMAIL).isEmpty()) {
                users.save(new AppUser(
                "Admin",
                ADMIN_EMAIL,
                encoder.encode(devAdminPassword()),
                Set.of(Role.ADMIN)
                ));
        }

        // PROMOTER
        if (users.findByEmail(PROMOTER_EMAIL).isEmpty()) {
                users.save(new AppUser(
                "Promoter",
                PROMOTER_EMAIL,
                encoder.encode(devUserPassword()),
                Set.of(Role.PROMOTER)
                ));
        }

        // VOLUNTEER
        if (users.findByEmail(VOL_EMAIL).isEmpty()) {
                users.save(new AppUser(
                "Volunteer",
                VOL_EMAIL,
                encoder.encode(devUserPassword()),
                Set.of(Role.VOLUNTEER)
                ));
        }
        }

    private void seedRewards() {
        if (rewards.count() > 0) return;

        rewards.save(new Reward("Coffee Voucher", 7));
        rewards.save(new Reward("Snack", 2));
        rewards.save(new Reward("T-Shirt", 20));
    }

    private void seedOpportunities() {
        if (opps.count() > 0) return;

        AppUser promoter = users.findByEmail(PROMOTER_EMAIL)
                .orElseThrow(() -> new IllegalStateException("Promoter seed missing"));

        Opportunity o1 = new Opportunity("Beach Cleanup", "Clean the beach",
                LocalDate.now().plusDays(10), 4, 10, promoter);
        Opportunity o2 = new Opportunity("Tree Planting", "Plant trees",
                LocalDate.now().plusDays(20), 3, 5, promoter);
        Opportunity o3 = new Opportunity("Food Drive", "Collect donations",
                LocalDate.now().plusDays(30), 2, 8, promoter);

        opps.save(o1);
        opps.save(o2);
        opps.save(o3);
    }
}
