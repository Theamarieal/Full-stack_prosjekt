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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ChecklistControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository orgRepository;

    @Autowired
    private ChecklistRepository checklistRepository;

    @Autowired
    private ChecklistItemRepository itemRepository;

    private Organization orgA;
    private Organization orgB;
    private User employee;
    private User manager;

    private void setMockUser(User user) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @BeforeEach
    void setUp() {
        orgA = orgRepository.save(Organization.builder().name("Everest Sushi").build());
        orgB = orgRepository.save(Organization.builder().name("Other Restaurant").build());

        employee = userRepository.save(User.builder()
                .email("employee@sushi.no").password("123").role(Role.EMPLOYEE)
                .organization(orgA).active(true).build());
        manager = userRepository.save(User.builder()
                .email("manager@sushi.no").password("123").role(Role.MANAGER)
                .organization(orgA).active(true).build());

        Checklist listA = checklistRepository.save(Checklist.builder()
                .title("Sushi Prep").organization(orgA).build());
        checklistRepository.save(Checklist.builder()
                .title("Secret list").organization(orgB).build());

        itemRepository.save(ChecklistItem.builder()
                .description("Mop floor").checklist(listA).completed(false).build());
    }

    // ── 401 / 403 access control ──────────────────────────────────────────

    @Test
    void getChecklists_withoutAuthentication_shouldReturn403() throws Exception {
        // Spring Security returns 403 for missing tokens (no WWW-Authenticate header configured)
        mockMvc.perform(get("/api/v1/checklists"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "employee@sushi.no", roles = "EMPLOYEE")
    void createChecklist_asEmployee_shouldReturn403() throws Exception {
        ChecklistRequest request = new ChecklistRequest();
        request.setTitle("Unauthorized list");

        mockMvc.perform(post("/api/v1/checklists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ── Organization isolation ────────────────────────────────────────────

    @Test
    void getChecklists_asEmployee_shouldOnlySeeOwnOrganization() throws Exception {
        setMockUser(employee);

        mockMvc.perform(get("/api/v1/checklists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Sushi Prep")));
    }

    // ── Manager can create ────────────────────────────────────────────────

    @Test
    void createChecklist_asManager_shouldSucceedAndBelongToOwnOrg() throws Exception {
        setMockUser(manager);

        ChecklistRequest request = new ChecklistRequest();
        request.setTitle("Manager's new list");
        request.setFrequency(Frequency.DAILY);

        mockMvc.perform(post("/api/v1/checklists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Manager's new list")));
    }

    // ── Input validation ──────────────────────────────────────────────────

    @Test
    void createChecklist_withBlankTitle_shouldReturn400() throws Exception {
        setMockUser(manager);

        ChecklistRequest request = new ChecklistRequest();
        request.setTitle(""); // blank — should fail @NotBlank

        mockMvc.perform(post("/api/v1/checklists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasItem(containsString("title"))));
    }

    // ── Complete item ─────────────────────────────────────────────────────

    @Test
    void completeItem_asEmployee_shouldMarkItemAsCompleted() throws Exception {
        setMockUser(employee);

        Long itemId = itemRepository.findAll().get(0).getId();

        mockMvc.perform(patch("/api/v1/checklists/1/items/" + itemId + "/complete"))
                .andExpect(status().isOk());

        // Verify item is marked completed in the database
        ChecklistItem item = itemRepository.findById(itemId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(item.isCompleted());
        org.junit.jupiter.api.Assertions.assertNotNull(item.getCompletedAt());
    }

    @Test
    void completeItem_fromDifferentOrg_shouldReturn400() throws Exception {
        // Create a checklist item belonging to orgB
        Checklist listB = checklistRepository.save(Checklist.builder()
                .title("OrgB list").organization(orgB).build());
        ChecklistItem itemB = itemRepository.save(ChecklistItem.builder()
                .description("OrgB task").checklist(listB).completed(false).build());

        // Try to complete it as an employee from orgA
        setMockUser(employee);

        mockMvc.perform(patch("/api/v1/checklists/1/items/" + itemB.getId() + "/complete"))
                .andExpect(status().isBadRequest());
    }
}
