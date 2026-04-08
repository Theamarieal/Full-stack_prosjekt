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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ntnu.no.fs_v26.model.TrainingCompletionType;
import ntnu.no.fs_v26.model.TrainingDocument;
import ntnu.no.fs_v26.model.TrainingDocumentType;
import ntnu.no.fs_v26.repository.TrainingDocumentRepository;

import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MultiTenancyIT {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private OrganizationRepository orgRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private DeviationRepository deviationRepository;

  @Autowired
  private ChecklistRepository checklistRepository;

  @Autowired
  private EquipmentRepository equipmentRepository;

  @Autowired
  private TemperatureLogRepository temperatureLogRepository;

  @Autowired
  private ObjectMapper objectMapper;

  private User userOrgA;
  private User userOrgB;
  private Organization orgA;
  private Organization orgB;

  @BeforeEach
  void setUp() {
    orgA = orgRepository.save(Organization.builder().name("Restaurant A").build());
    orgB = orgRepository.save(Organization.builder().name("Restaurant B").build());

    userOrgA = userRepository.save(User.builder()
        .email("employee@orgA.no")
        .password("pw")
        .role(Role.EMPLOYEE)
        .organization(orgA)
        .build());

    userOrgB = userRepository.save(User.builder()
        .email("employee@orgB.no")
        .password("pw")
        .role(Role.EMPLOYEE)
        .organization(orgB)
        .build());

    // Data for orgA
    deviationRepository.save(Deviation.builder()
        .title("Deviation in org A")
        .status(DeviationStatus.OPEN)
        .module(DeviationModule.IK_MAT)
        .createdAt(LocalDateTime.now())
        .organization(orgA)
        .reportedBy(userOrgA)
        .build());

    checklistRepository.save(Checklist.builder()
        .title("Checklist in org A")
        .organization(orgA)
        .build());

    // Data for orgB
    deviationRepository.save(Deviation.builder()
        .title("Deviation in org B")
        .status(DeviationStatus.OPEN)
        .module(DeviationModule.IK_MAT)
        .createdAt(LocalDateTime.now())
        .organization(orgB)
        .reportedBy(userOrgB)
        .build());

    checklistRepository.save(Checklist.builder()
        .title("Checklist in org B")
        .organization(orgB)
        .build());
  }

  // ── Deviations ────────────────────────────────────────────────────────────

  @Test
  void deviations_userFromOrgA_shouldOnlySeeOrgAData() throws Exception {
    mockMvc.perform(get("/api/v1/deviations")
            .with(user(userOrgA)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].title", is("Deviation in org A")));
  }

  @Test
  void deviations_userFromOrgB_shouldOnlySeeOrgBData() throws Exception {
    mockMvc.perform(get("/api/v1/deviations")
            .with(user(userOrgB)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].title", is("Deviation in org B")));
  }

  @Test
  void deviations_managerFromOrgB_cannotUpdateStatusOfOrgADeviation() throws Exception {
    Deviation orgADeviation = deviationRepository
        .findAllByOrganizationId(orgA.getId()).get(0);

    User managerOrgB = userRepository.save(User.builder()
        .email("manager@orgB.no")
        .password("pw")
        .role(Role.MANAGER)
        .organization(orgB)
        .build());

    mockMvc.perform(patch("/api/v1/deviations/" + orgADeviation.getId() + "/status")
            .param("status", "RESOLVED")
            .with(user(managerOrgB)))
        .andExpect(status().isForbidden());
  }

  // ── Checklists ────────────────────────────────────────────────────────────

  @Test
  void checklists_userFromOrgA_shouldOnlySeeOrgAData() throws Exception {
    mockMvc.perform(get("/api/v1/checklists")
            .with(user(userOrgA)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].title", is("Checklist in org A")));
  }

  @Test
  void checklists_userFromOrgB_shouldOnlySeeOrgBData() throws Exception {
    mockMvc.perform(get("/api/v1/checklists")
            .with(user(userOrgB)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].title", is("Checklist in org B")));
  }

  // ── Temperature logs ──────────────────────────────────────────────────────

  @Test
  void temperatureLogs_userFromOrgA_shouldOnlySeeOrgAData() throws Exception {
    Equipment equipA = equipmentRepository.save(Equipment.builder()
        .name("Fridge A")
        .minTemp(0).maxTemp(4)
        .organization(orgA)
        .build());

    temperatureLogRepository.save(TemperatureLog.builder()
        .value(3.0)
        .timestamp(LocalDateTime.now())
        .isDeviation(false)
        .equipment(equipA)
        .loggedBy(userOrgA)
        .build());

    mockMvc.perform(get("/api/v1/temperature-logs")
            .with(user(userOrgA)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  // ── Training documents ────────────────────────────────────────────────────

  @Autowired
  private TrainingDocumentRepository trainingDocumentRepository;

  @Test
  void trainingPolicies_userFromOrgA_shouldOnlySeeOrgADocuments() throws Exception {
    trainingDocumentRepository.save(TrainingDocument.builder()
        .title("Policy in org A")
        .content("content")
        .type(TrainingDocumentType.POLICY)
        .completionType(TrainingCompletionType.READ_ACKNOWLEDGE)
        .organization(orgA)
        .active(true)
        .build());

    trainingDocumentRepository.save(TrainingDocument.builder()
        .title("Policy in org B")
        .content("content")
        .type(TrainingDocumentType.POLICY)
        .completionType(TrainingCompletionType.READ_ACKNOWLEDGE)
        .organization(orgB)
        .active(true)
        .build());

    mockMvc.perform(get("/api/v1/training/policies")
            .with(user(userOrgA)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].title", is("Policy in org A")));
  }

  // ── Admin ─────────────────────────────────────────────────────────────────

  @Test
  void admin_createUser_organizationTakenFromJwt_notFromBody() throws Exception {
    User adminOrgA = userRepository.save(User.builder()
        .email("admin@orgA.no")
        .password("pw")
        .role(Role.ADMIN)
        .organization(orgA)
        .build());

    Map<String, Object> body = Map.of(
        "email", "newuser@test.no",
        "password", "Password1",
        "role", "EMPLOYEE"
    );

    mockMvc.perform(post("/api/v1/admin/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body))
            .with(user(adminOrgA)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email", is("newuser@test.no")));

    // Verify new user belongs to orgA
    User newUser = userRepository.findByEmail("newuser@test.no").orElseThrow();
    org.junit.jupiter.api.Assertions.assertEquals(orgA.getId(), newUser.getOrganization().getId());
  }

  @Test
  void admin_getAllUsers_shouldOnlySeeUsersFromOwnOrganization() throws Exception {
    User adminOrgA = userRepository.save(User.builder()
        .email("admin@orgA.no")
        .password("pw")
        .role(Role.ADMIN)
        .organization(orgA)
        .build());

    mockMvc.perform(get("/api/v1/admin/users")
            .with(user(adminOrgA)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].email", not(hasItem("employee@orgB.no"))));
  }

  // ── organizationId must never be accepted from request body ───────────────

  @Test
  void deviations_requestBodyOrganizationIdIsIgnored_savedUnderAuthenticatedUsersOrg() throws Exception {
    Map<String, Object> body = Map.of(
        "title", "Sneaky deviation",
        "description", "Trying to assign to org B",
        "module", "IK_MAT",
        "organizationId", orgB.getId()
    );

    mockMvc.perform(post("/api/v1/deviations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body))
            .with(user(userOrgA)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.organization.name", is("Restaurant A")));
  }

  // ── Unauthenticated access ────────────────────────────────────────────────

  @Test
  void deviations_withoutAuthentication_shouldReturn403() throws Exception {
    mockMvc.perform(get("/api/v1/deviations"))
        .andExpect(status().isForbidden());
  }

  @Test
  void checklists_withoutAuthentication_shouldReturn403() throws Exception {
    mockMvc.perform(get("/api/v1/checklists"))
        .andExpect(status().isForbidden());
  }
}