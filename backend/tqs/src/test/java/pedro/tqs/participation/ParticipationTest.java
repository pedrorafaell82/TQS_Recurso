package pedro.tqs.participation;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import pedro.tqs.opportunity.OpportunityRepository;
import pedro.tqs.user.*;

import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ParticipationTest {

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired OpportunityRepository opps;
    @Autowired ParticipationRepository participations;

    @BeforeEach
    void clean() {
        participations.deleteAll();
        opps.deleteAll();
        users.deleteAll();
        users.save(new AppUser(
                "Admin",
                "admin@local.test",
                new BCryptPasswordEncoder().encode("adminPass1"),
                Set.of(Role.ADMIN)
        ));
    }

    private Long register(String name, String email) throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"%s","email":"%s","password":"strongPass1"}
                """.formatted(name, email)))
            .andExpect(status().isCreated());
        return users.findByEmail(email).orElseThrow().getId();
    }

    private void promoteToPromoter(Long userId) throws Exception {
        mvc.perform(put("/api/admin/users/" + userId + "/roles")
                .with(httpBasic("admin@local.test", "adminPass1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"roles":["PROMOTER"]}"""))
            .andExpect(status().isNoContent());
    }

    private Long createOpportunityAsPromoter(String promoterEmail) throws Exception {
        mvc.perform(post("/api/opportunities")
                .with(httpBasic(promoterEmail, "strongPass1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title":"Beach","description":"Cleanup","date":"2026-02-10","durationHours":4,"points":10}
                """))
            .andExpect(status().isCreated());
        return opps.findAll().get(0).getId();
    }


    @Test
    void enroll_asVolunteer_created() throws Exception {
        Long promoterId = register("Promoter", "promoter@test.com");
        promoteToPromoter(promoterId);
        Long oppId = createOpportunityAsPromoter("promoter@test.com");

        register("Vol", "vol@test.com");

        mvc.perform(post("/api/opportunities/" + oppId + "/enroll")
                .with(httpBasic("vol@test.com", "strongPass1")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.opportunityId").value(oppId))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void enroll_twice_conflict() throws Exception {
        Long promoterId = register("Promoter", "promoter@test.com");
        promoteToPromoter(promoterId);
        Long oppId = createOpportunityAsPromoter("promoter@test.com");

        register("Vol", "vol@test.com");

        mvc.perform(post("/api/opportunities/" + oppId + "/enroll")
                .with(httpBasic("vol@test.com", "strongPass1")))
            .andExpect(status().isCreated());

        mvc.perform(post("/api/opportunities/" + oppId + "/enroll")
                .with(httpBasic("vol@test.com", "strongPass1")))
            .andExpect(status().isConflict());
    }

    @Test
    void enroll_asPromoter_forbidden() throws Exception {
        Long promoterId = register("Promoter", "promoter@test.com");
        promoteToPromoter(promoterId);
        Long oppId = createOpportunityAsPromoter("promoter@test.com");

        mvc.perform(post("/api/opportunities/" + oppId + "/enroll")
                .with(httpBasic("promoter@test.com", "strongPass1")))
            .andExpect(status().isForbidden());
    }

    @Test
    void enroll_nonExistingOpportunity_returns404() throws Exception {
        register("Vol", "vol@test.com");

        mvc.perform(post("/api/opportunities/99999/enroll")
                .with(httpBasic("vol@test.com", "strongPass1")))
            .andExpect(status().isNotFound());
    }

    @Test
    void approveParticipation_asOwner_changesStatusToApproved() throws Exception {
        Long promoterId = register("Promoter", "promoter@test.com");
        promoteToPromoter(promoterId);
        Long oppId = createOpportunityAsPromoter("promoter@test.com");
        register("Vol", "vol@test.com");

        mvc.perform(post("/api/opportunities/" + oppId + "/enroll")
                .with(httpBasic("vol@test.com", "strongPass1")))
            .andExpect(status().isCreated());

        Long partId = participations.findAll().get(0).getId();

        mvc.perform(post("/api/participations/" + partId + "/approve")
                .with(httpBasic("promoter@test.com", "strongPass1")))
            .andExpect(status().isNoContent());

        Assertions.assertEquals(ParticipationStatus.APPROVED,
                participations.findById(partId).orElseThrow().getStatus());
    }

    @Test
    void rejectParticipation_asOwner_changesStatusToRejected() throws Exception {
        Long promoterId = register("Promoter", "promoter@test.com");
        promoteToPromoter(promoterId);
        Long oppId = createOpportunityAsPromoter("promoter@test.com");
        register("Vol", "vol@test.com");

        mvc.perform(post("/api/opportunities/" + oppId + "/enroll")
                .with(httpBasic("vol@test.com", "strongPass1")))
            .andExpect(status().isCreated());

        Long partId = participations.findAll().get(0).getId();

        mvc.perform(post("/api/participations/" + partId + "/reject")
                .with(httpBasic("promoter@test.com", "strongPass1")))
            .andExpect(status().isNoContent());

        Assertions.assertEquals(ParticipationStatus.REJECTED,
                participations.findById(partId).orElseThrow().getStatus());
    }

    @Test
    void approveParticipation_asVolunteer_forbidden() throws Exception {
        Long promoterId = register("Promoter", "promoter@test.com");
        promoteToPromoter(promoterId);
        Long oppId = createOpportunityAsPromoter("promoter@test.com");
        register("Vol", "vol@test.com");

        mvc.perform(post("/api/opportunities/" + oppId + "/enroll")
                .with(httpBasic("vol@test.com", "strongPass1")))
            .andExpect(status().isCreated());

        Long partId = participations.findAll().get(0).getId();

        mvc.perform(post("/api/participations/" + partId + "/approve")
                .with(httpBasic("vol@test.com", "strongPass1")))
            .andExpect(status().isForbidden());
    }

    @Test
    void enroll_inInactiveOpportunity_returns400() throws Exception {
        Long promoterId = register("Promoter", "promoter@test.com");
        promoteToPromoter(promoterId);
        Long oppId = createOpportunityAsPromoter("promoter@test.com");

        mvc.perform(patch("/api/opportunities/" + oppId + "/deactivate")
                .with(httpBasic("promoter@test.com", "strongPass1")))
            .andExpect(status().isNoContent());

        register("Vol", "vol@test.com");

        mvc.perform(post("/api/opportunities/" + oppId + "/enroll")
                .with(httpBasic("vol@test.com", "strongPass1")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void approveParticipation_whenNotPending_returns409() throws Exception {
        Long promoterId = register("Promoter", "promoter@test.com");
        promoteToPromoter(promoterId);
        Long oppId = createOpportunityAsPromoter("promoter@test.com");
        register("Vol", "vol@test.com");

        mvc.perform(post("/api/opportunities/" + oppId + "/enroll")
                .with(httpBasic("vol@test.com", "strongPass1")))
            .andExpect(status().isCreated());

        Long partId = participations.findAll().get(0).getId();

        mvc.perform(post("/api/participations/" + partId + "/approve")
                .with(httpBasic("promoter@test.com", "strongPass1")))
            .andExpect(status().isNoContent());

        mvc.perform(post("/api/participations/" + partId + "/approve")
                .with(httpBasic("promoter@test.com", "strongPass1")))
            .andExpect(status().isConflict());
    }

    @Test
    void rejectParticipation_whenNotPending_returns409() throws Exception {
        Long promoterId = register("Promoter", "promoter@test.com");
        promoteToPromoter(promoterId);
        Long oppId = createOpportunityAsPromoter("promoter@test.com");
        register("Vol", "vol@test.com");

        mvc.perform(post("/api/opportunities/" + oppId + "/enroll")
                .with(httpBasic("vol@test.com", "strongPass1")))
            .andExpect(status().isCreated());

        Long partId = participations.findAll().get(0).getId();

        mvc.perform(post("/api/participations/" + partId + "/reject")
                .with(httpBasic("promoter@test.com", "strongPass1")))
            .andExpect(status().isNoContent());

        mvc.perform(post("/api/participations/" + partId + "/reject")
                .with(httpBasic("promoter@test.com", "strongPass1")))
            .andExpect(status().isConflict());
    }

    @Test
    void approveParticipation_asOtherPromoter_forbidden() throws Exception {
        Long promoter1 = register("Promoter1", "p1@test.com");
        promoteToPromoter(promoter1);
        Long oppId = createOpportunityAsPromoter("p1@test.com");

        Long promoter2 = register("Promoter2", "p2@test.com");
        promoteToPromoter(promoter2);

        register("Vol", "vol@test.com");

        mvc.perform(post("/api/opportunities/" + oppId + "/enroll")
                .with(httpBasic("vol@test.com", "strongPass1")))
            .andExpect(status().isCreated());

        Long partId = participations.findAll().get(0).getId();

        mvc.perform(post("/api/participations/" + partId + "/approve")
                .with(httpBasic("p2@test.com", "strongPass1")))
            .andExpect(status().isForbidden());
    }

    @Test
    void reenroll_afterRejected_setsPendingAgain() throws Exception {
        Long promoterId = register("Promoter", "promoter@test.com");
        promoteToPromoter(promoterId);
        Long oppId = createOpportunityAsPromoter("promoter@test.com");
        register("Vol", "vol@test.com");
        mvc.perform(post("/api/opportunities/" + oppId + "/enroll")
                .with(httpBasic("vol@test.com", "strongPass1")))
            .andExpect(status().isCreated());

        Long partId = participations.findAll().get(0).getId();
        mvc.perform(post("/api/participations/" + partId + "/reject")
                .with(httpBasic("promoter@test.com", "strongPass1")))
            .andExpect(status().isNoContent());

        mvc.perform(post("/api/opportunities/" + oppId + "/enroll")
                .with(httpBasic("vol@test.com", "strongPass1")))
            .andExpect(status().isConflict());
    }

    @Test
    void getMyParticipations_asVolunteer_returnsList() throws Exception {
        Long promoterId = register("Promoter", "promoter@test.com");
        promoteToPromoter(promoterId);
        Long oppId = createOpportunityAsPromoter("promoter@test.com");

        register("Vol", "vol@test.com");

        mvc.perform(post("/api/opportunities/" + oppId + "/enroll")
                .with(httpBasic("vol@test.com", "strongPass1")))
            .andExpect(status().isCreated());

        mvc.perform(get("/api/participations/me")
                .with(httpBasic("vol@test.com", "strongPass1")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].opportunityId").value(oppId))
            .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getMyParticipations_noParticipations_returnsEmptyList() throws Exception {
        register("Vol", "vol@test.com");

        mvc.perform(get("/api/participations/me")
                .with(httpBasic("vol@test.com", "strongPass1")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getMyParticipations_asPromoter_forbidden() throws Exception {
        Long promoterId = register("Promoter", "promoter@test.com");
        promoteToPromoter(promoterId);

        mvc.perform(get("/api/participations/me")
                .with(httpBasic("promoter@test.com", "strongPass1")))
            .andExpect(status().isForbidden());
    }
}
