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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // roll back databasen for each test
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

    private void setMockUser(User user) {
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            user, null, user.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @BeforeEach
    void setUp() {
        // make organization A and B
        orgA = orgRepository.save(Organization.builder().name("Everest Sushi").build());
        orgB = orgRepository.save(Organization.builder().name("Other Restaurant").build());

        // make users for Org A
        userRepository.save(User.builder()
            .email("employee@sushi.no").password("123").role(Role.EMPLOYEE).organization(orgA).build());
        userRepository.save(User.builder()
            .email("manager@sushi.no").password("123").role(Role.MANAGER).organization(orgA).build());
        
            // make a checklist for each organization
        Checklist listA = checklistRepository.save(Checklist.builder()
                .title("Sushi Prep").organization(orgA).build());
        checklistRepository.save(Checklist.builder()
        .title("Secret list").organization(orgB).build());

        itemRepository.save(ChecklistItem.builder().description("Mop floor").checklist(listA).build());
    }

    @Test
    void employeeShouldOnlySeeOwnOrganizationChecklists() throws Exception {
        // Finn brukeren vi lagde i setUp()
        User user = userRepository.findByEmail("employee@sushi.no").orElseThrow();
        setMockUser(user); // <--- Tvinger Spring til å bruke denne ekte brukeren

        mockMvc.perform(get("/api/v1/checklists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Sushi Prep")));
    }

    @Test
    @WithMockUser(username = "employee@sushi.no", roles = "EMPLOYEE")
    void employeeShouldBeForbiddenFromCreatingChecklist() throws Exception {
        // tests that employee CAN NOT make a new list (only Manager/Admin) 
        Checklist newList = Checklist.builder().title("New list").build();

        mockMvc.perform(post("/api/v1/checklists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(newList)))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCanCreateChecklistForOwnOrg() throws Exception {
        User user = userRepository.findByEmail("manager@sushi.no").orElseThrow();
        setMockUser(user);

        Checklist newList = Checklist.builder().title("Manager sin liste").build();

        mockMvc.perform(post("/api/v1/checklists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(newList)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Manager sin liste")));
    }

    @Test
    void employeeCanCompleteItemInOwnOrg() throws Exception {
        User user = userRepository.findByEmail("employee@sushi.no").orElseThrow();
        setMockUser(user);

        Long itemId = itemRepository.findAll().get(0).getId();

        mockMvc.perform(patch("/api/v1/checklists/1/items/" + itemId + "/complete"))
                .andExpect(status().isOk());
    }
}