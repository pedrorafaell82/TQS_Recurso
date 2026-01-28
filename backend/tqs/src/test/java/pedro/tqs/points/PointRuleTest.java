package pedro.tqs.points;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

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
class PointRulesTest {

    @Autowired MockMvc mvc;

    @Autowired UserRepository users;
    @Autowired PointTransactionRepository pointTx;
    @Autowired PointRuleConfigRepository rulesRepo;
    @Autowired pedro.tqs.participation.ParticipationRepository participations;
    @Autowired pedro.tqs.opportunity.OpportunityRepository opps;
    @Autowired pedro.tqs.reward.RewardRepository rewards;

    @BeforeEach
    void clean() {
        pointTx.deleteAll();
        participations.deleteAll();
        opps.deleteAll();
        rewards.deleteAll();
        rulesRepo.deleteAll();
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

    private Long register(String name, String email) throws Exception {
        String location = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"%s","email":"%s","password":"strongPass1"}
                        """.formatted(name, email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        Assertions.assertNotNull(location);
        return Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
    }

    @Test
    void getRules_defaultIsOne() throws Exception {
        mvc.perform(get("/api/admin/points/rules")
                        .with(httpBasic("admin@local.test", "adminPass1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalMultiplier").value(1.0));
    }

    @Test
    void updateRules_adminCanSetMultiplier() throws Exception {
        mvc.perform(put("/api/admin/points/rules")
                        .with(httpBasic("admin@local.test", "adminPass1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"approvalMultiplier":2.0}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalMultiplier").value(2.0));

        mvc.perform(get("/api/admin/points/rules")
                        .with(httpBasic("admin@local.test", "adminPass1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalMultiplier").value(2.0));
    }

    @Test
    void updateRules_nonAdmin_forbidden() throws Exception {
        register("Vol", "vol@test.com");

        mvc.perform(put("/api/admin/points/rules")
                        .with(httpBasic("vol@test.com", "strongPass1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"approvalMultiplier":2.0}"""))
                .andExpect(status().isForbidden());
    }
}
