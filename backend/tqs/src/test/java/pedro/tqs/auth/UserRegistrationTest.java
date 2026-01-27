package pedro.tqs.auth;

import pedro.tqs.opportunity.OpportunityRepository;
import pedro.tqs.participation.ParticipationRepository;
import pedro.tqs.points.PointTransactionRepository;
import pedro.tqs.user.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserRegistrationTest {

    @Autowired MockMvc mvc;
    @Autowired UserRepository userRepository;
    @Autowired ParticipationRepository participations;
    @Autowired OpportunityRepository opps;
    @Autowired PointTransactionRepository pointTx;

    @BeforeEach
    void clean() {
        pointTx.deleteAll();
        participations.deleteAll();
        opps.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void register_success_returns201() throws Exception {
        String body = """
            { "name": "Pedro", "email": "pedro@example.com", "password": "strongPass1" }
        """;

        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.email").value("pedro@example.com"))
            .andExpect(jsonPath("$.roles", hasItem("VOLUNTEER")));
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        String body = """
            { "name": "Pedro", "email": "pedro@example.com", "password": "strongPass1" }
        """;

        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated());

        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.title").value("Email already exists"));

    }

    @Test
    void me_withoutCredentials_returns401() throws Exception {
        mvc.perform(get("/api/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void me_withCredentials_returns200() throws Exception {
        String body = """
            { "name": "Pedro", "email": "pedro@example.com", "password": "strongPass1" }
        """;

        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated());

        mvc.perform(get("/api/me").with(httpBasic("pedro@example.com", "strongPass1")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("pedro@example.com"))
            .andExpect(jsonPath("$.roles", hasItem("VOLUNTEER")));
    }

    @Test
    void register_invalidPayload_returns400_withErrors() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"name":"","email":"not-an-email","password":"x"}
                """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Validation failed"))
            .andExpect(jsonPath("$.errors").exists());
    }
}
