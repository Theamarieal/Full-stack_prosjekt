package ntnu.no.fs_v26.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ntnu.no.fs_v26.model.*;
import ntnu.no.fs_v26.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DeviationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeviationRepository deviationRepository;

    @Autowired
    private OrganizationRepository orgRepository;

    @Autowired
    private UserRepository userRepository;

    private User manager;
    private User employee;
    private Deviation openDeviation;

    private void setMockUser(User user) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @BeforeEach
    void setUp() {
        Organization org = orgRepository.save(Organization.builder().name("Test Org").build());

        manager = userRepository.save(User.builder()
                .email("manager@test.no").password("123").role(Role.MANAGER)
                .organization(org).active(true).build());

        employee = userRepository.save(User.builder()
                .email("worker@test.no").password("123").role(Role.EMPLOYEE)
                .organization(org).active(true).build());

        openDeviation = deviationRepository.save(Deviation.builder()
                .title("Old deviation")
                .status(DeviationStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .organization(org)
                .build());
    }

    // ── 403 access control ────────────────────────────────────────────────

    @Test
    void getDeviations_withoutAuthentication_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/deviations"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateStatus_asEmployee_shouldReturn403() throws Exception {
        setMockUser(employee);

        mockMvc.perform(patch("/api/v1/deviations/" + openDeviation.getId() + "/status")
                        .param("status", "RESOLVED"))
                .andExpect(status().isForbidden());
    }

    // ── Reporting a deviation ─────────────────────────────────────────────

    @Test
    void reportDeviation_asEmployee_shouldCreateWithStatusOpen() throws Exception {
        setMockUser(employee);

        DeviationRequest request = new DeviationRequest();
        request.setTitle("Leak");
        request.setDescription("Water on the floor");
        request.setModule(DeviationModule.IK_MAT);

        mockMvc.perform(post("/api/v1/deviations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OPEN")))
                .andExpect(jsonPath("$.title", is("Leak")))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    void reportDeviation_withBlankTitle_shouldReturn400() throws Exception {
        setMockUser(employee);

        DeviationRequest request = new DeviationRequest();
        request.setTitle(""); // blank — @NotBlank should reject this

        mockMvc.perform(post("/api/v1/deviations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasItem(containsString("title"))));
    }

    // ── Status workflow: OPEN → IN_PROGRESS → RESOLVED ───────────────────

    @Test
    void statusWorkflow_openToInProgress_shouldSucceed() throws Exception {
        setMockUser(manager);

        mockMvc.perform(patch("/api/v1/deviations/" + openDeviation.getId() + "/status")
                        .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));
    }

    @Test
    void statusWorkflow_openToResolved_shouldSetResolvedAt() throws Exception {
        setMockUser(manager);

        mockMvc.perform(patch("/api/v1/deviations/" + openDeviation.getId() + "/status")
                        .param("status", "RESOLVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESOLVED")))
                .andExpect(jsonPath("$.resolvedAt", notNullValue()));

        // Verify resolvedAt is persisted in the database
        Deviation updated = deviationRepository.findById(openDeviation.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertNotNull(updated.getResolvedAt());
    }

    @Test
    void statusWorkflow_fullFlow_openInProgressResolved() throws Exception {
        setMockUser(manager);

        // Step 1: OPEN → IN_PROGRESS
        mockMvc.perform(patch("/api/v1/deviations/" + openDeviation.getId() + "/status")
                        .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));

        // Step 2: IN_PROGRESS → RESOLVED
        mockMvc.perform(patch("/api/v1/deviations/" + openDeviation.getId() + "/status")
                        .param("status", "RESOLVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESOLVED")))
                .andExpect(jsonPath("$.resolvedAt", notNullValue()));
    }

    // ── Organization isolation ────────────────────────────────────────────

    @Test
    void getDeviations_shouldOnlySeeOwnOrganization() throws Exception {
        Organization orgB = orgRepository.save(Organization.builder().name("Other Org").build());
        deviationRepository.save(Deviation.builder()
                .title("Secret deviation")
                .status(DeviationStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .organization(orgB)
                .build());

        setMockUser(employee);

        mockMvc.perform(get("/api/v1/deviations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Old deviation")));
    }

    @Test
    void updateStatus_deviationFromOtherOrg_shouldReturn400() throws Exception {
        Organization orgB = orgRepository.save(Organization.builder().name("Other Org").build());
        Deviation orgBDeviation = deviationRepository.save(Deviation.builder()
                .title("Org B deviation")
                .status(DeviationStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .organization(orgB)
                .build());

        // Manager from orgA tries to update a deviation belonging to orgB
        setMockUser(manager);

        mockMvc.perform(patch("/api/v1/deviations/" + orgBDeviation.getId() + "/status")
                        .param("status", "RESOLVED"))
                .andExpect(status().isBadRequest());
    }
}
