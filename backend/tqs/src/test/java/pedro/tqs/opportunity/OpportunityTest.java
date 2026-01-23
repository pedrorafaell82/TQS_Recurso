package pedro.tqs.opportunity;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pedro.tqs.user.*;

import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OpportunityTest {

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired OpportunityRepository opps;

    @BeforeEach
    void clean() {
        opps.deleteAll();
        users.deleteAll();
        users.save(new AppUser("Admin", "admin@local.test", new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("adminPass1"), Set.of(Role.ADMIN)));
    }

    private Long registerVolunteerAndPromote() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"PromoterUser","email":"promoter@test.com","password":"strongPass1"}"""))
            .andExpect(status().isCreated());

        AppUser u = users.findByEmail("promoter@test.com").orElseThrow();

        mvc.perform(put("/api/admin/users/" + u.getId() + "/roles")
                .with(httpBasic("admin@local.test", "adminPass1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"roles":["PROMOTER"]}"""))
            .andExpect(status().isNoContent());

        return u.getId();
    }

    @Test
    void createOpportunity_asVolunteer_forbidden() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Vol","email":"vol@test.com","password":"strongPass1"}
                """))
            .andExpect(status().isCreated());

        mvc.perform(post("/api/opportunities")
                .with(httpBasic("vol@test.com", "strongPass1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title":"Beach","description":"Cleanup","date":"2026-02-10","durationHours":4,"points":10}
                """))
            .andExpect(status().isForbidden());
    }

    @Test
    void createOpportunity_asPromoter_created_and_browse_lists_it() throws Exception {
        registerVolunteerAndPromote();

        mvc.perform(post("/api/opportunities")
                .with(httpBasic("promoter@test.com", "strongPass1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title":"Beach","description":"Cleanup","date":"2026-02-10","durationHours":4,"points":10}
                """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Beach"));

        mvc.perform(get("/api/opportunities")
                .with(httpBasic("promoter@test.com", "strongPass1")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("Beach"));
    }

    @Test
    void assignRoles_requiresAdmin() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"User","email":"user@test.com","password":"strongPass1"}
                """))
            .andExpect(status().isCreated());

        AppUser u = users.findByEmail("user@test.com").orElseThrow();

        mvc.perform(put("/api/admin/users/" + u.getId() + "/roles")
                .with(httpBasic("user@test.com", "strongPass1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"roles":["PROMOTER"]}
                """))
            .andExpect(status().isForbidden());
    }

    @Test
    void browseOpportunities_withoutAuth_unauthorized() throws Exception {
        mvc.perform(get("/api/opportunities"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void afterAdminPromotesUser_userCanCreateOpportunity() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Prom","email":"p2@test.com","password":"strongPass1"}
                """))
            .andExpect(status().isCreated());

        AppUser u = users.findByEmail("p2@test.com").orElseThrow();

        mvc.perform(put("/api/admin/users/" + u.getId() + "/roles")
                .with(httpBasic("admin@local.test", "adminPass1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"roles":["PROMOTER"]}
                """))
            .andExpect(status().isNoContent());

        mvc.perform(post("/api/opportunities")
                .with(httpBasic("p2@test.com", "strongPass1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title":"Tree Planting","description":"Plant trees","date":"2026-02-11","durationHours":2,"points":5}
                """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Tree Planting"));
    }

    @Test
    void createOpportunity_invalidPayload_returns400() throws Exception {
        registerVolunteerAndPromote();

        mvc.perform(post("/api/opportunities")
                .with(httpBasic("promoter@test.com", "strongPass1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title":"","description":"Cleanup","date":"2026-02-10","durationHours":0,"points":10}
                """))
            .andExpect(status().isBadRequest());
    }

}
