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

import pedro.tqs.opportunity.Opportunity;
import pedro.tqs.opportunity.OpportunityRepository;
import pedro.tqs.participation.Participation;
import pedro.tqs.participation.ParticipationRepository;
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
class AwardPointsUsesMultiplierTest {

    @Autowired MockMvc mvc;

    @Autowired UserRepository users;
    @Autowired OpportunityRepository opps;
    @Autowired ParticipationRepository participations;
    @Autowired PointTransactionRepository pointTx;
    @Autowired PointRuleConfigRepository rulesRepo;

    @BeforeEach
    void clean() {
        pointTx.deleteAll();
        participations.deleteAll();
        opps.deleteAll();
        rulesRepo.deleteAll();
        users.deleteAll();
        rulesRepo.deleteAll();
        rulesRepo.save(new PointRuleConfig(1.0));
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

    private void promoteToPromoter(Long userId) throws Exception {
        mvc.perform(put("/api/admin/users/" + userId + "/roles")
                        .with(httpBasic("admin@local.test", "adminPass1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"roles":["PROMOTER"]}"""))
                .andExpect(status().isNoContent());
    }

    private Long createOpportunityAsPromoter(String promoterEmail, int points) throws Exception {
        mvc.perform(post("/api/opportunities")
                        .with(httpBasic(promoterEmail, "strongPass1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"title":"Opp","description":"Cleanup","date":"2026-02-10","durationHours":4,"points":%d}
                        """.formatted(points)))
                .andExpect(status().isCreated());

        return opps.findAll().stream().map(Opportunity::getId).max(Long::compareTo).orElseThrow();
    }

    private Long enrollAsVolunteer(Long oppId, String volunteerEmail) throws Exception {
        mvc.perform(post("/api/opportunities/" + oppId + "/enroll")
                        .with(httpBasic(volunteerEmail, "strongPass1")))
                .andExpect(status().isCreated());

        return participations.findAll().stream().map(Participation::getId).max(Long::compareTo).orElseThrow();
    }

    @Test
    void approvalAwardsPointsUsingMultiplier() throws Exception {
        mvc.perform(put("/api/admin/points/rules")
                        .with(httpBasic("admin@local.test", "adminPass1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"approvalMultiplier":2.0}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalMultiplier").value(2.0));

        Long promoterId = register("Promoter", "promoter@test.com");
        promoteToPromoter(promoterId);
        Long oppId = createOpportunityAsPromoter("promoter@test.com", 10);

        register("Vol", "vol@test.com");
        Long partId = enrollAsVolunteer(oppId, "vol@test.com");

        mvc.perform(post("/api/participations/" + partId + "/approve")
                        .with(httpBasic("promoter@test.com", "strongPass1")))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/points/balance")
                        .with(httpBasic("vol@test.com", "strongPass1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(20));
    }
}
