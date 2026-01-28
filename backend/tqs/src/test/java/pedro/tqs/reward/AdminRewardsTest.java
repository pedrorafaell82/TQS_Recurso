package pedro.tqs.reward;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import pedro.tqs.opportunity.OpportunityRepository;
import pedro.tqs.participation.ParticipationRepository;
import pedro.tqs.points.PointTransactionRepository;
import pedro.tqs.user.AppUser;
import pedro.tqs.user.Role;
import pedro.tqs.user.UserRepository;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminRewardsTest {

    @Autowired MockMvc mvc;

    @Autowired UserRepository users;
    @Autowired RewardRepository rewards;
    @Autowired OpportunityRepository opps;
    @Autowired ParticipationRepository participations;
    @Autowired PointTransactionRepository pointTx;

    @BeforeEach
    void clean() {
        pointTx.deleteAll();
        participations.deleteAll();
        opps.deleteAll();
        rewards.deleteAll();
        users.deleteAll();
        seedAdmin();
    }

    private void seedAdmin() {
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ADMIN);

        AppUser admin = new AppUser(
                "Admin",
                "admin@local.test",
                new BCryptPasswordEncoder().encode("adminPass1"),
                roles
        );
        users.save(admin);
    }

    private void registerVolunteer() throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"Vol","email":"vol@test.com","password":"strongPass1"}
                        """))
                .andExpect(status().isCreated());
    }

    @Test
    void createReward_admin_createsAndReturns201() throws Exception {
        mvc.perform(post("/api/admin/rewards")
                        .with(httpBasic("admin@local.test", "adminPass1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"Coffee Voucher","cost":7}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Coffee Voucher"))
                .andExpect(jsonPath("$.cost").value(7))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createReward_invalidPayload_returns400() throws Exception {
        mvc.perform(post("/api/admin/rewards")
                        .with(httpBasic("admin@local.test", "adminPass1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"   ","cost":0}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listAll_admin_includesInactive() throws Exception {
        Reward r = rewards.save(new Reward("Coffee", 7));
        r.setActive(false);
        rewards.save(r);

        mvc.perform(get("/api/admin/rewards")
                        .with(httpBasic("admin@local.test", "adminPass1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Coffee"))
                .andExpect(jsonPath("$[0].active").value(false));
    }

    @Test
    void updateReward_patch_updatesNameCostActive() throws Exception {
        Reward r = rewards.save(new Reward("Coffee", 7));

        mvc.perform(patch("/api/admin/rewards/" + r.getId())
                        .with(httpBasic("admin@local.test", "adminPass1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"Coffee Voucher","cost":10,"active":false}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(r.getId()))
                .andExpect(jsonPath("$.name").value("Coffee Voucher"))
                .andExpect(jsonPath("$.cost").value(10))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void updateReward_notFound_returns404() throws Exception {
        mvc.perform(patch("/api/admin/rewards/99999")
                        .with(httpBasic("admin@local.test", "adminPass1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"active":false}"""))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminEndpoints_nonAdmin_forbidden() throws Exception {
        registerVolunteer();

        mvc.perform(get("/api/admin/rewards")
                        .with(httpBasic("vol@test.com", "strongPass1")))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/admin/rewards")
                        .with(httpBasic("vol@test.com", "strongPass1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"X","cost":1}"""))
                .andExpect(status().isForbidden());
    }
}
