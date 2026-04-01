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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

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
    private Deviation testDeviation;

    @BeforeEach
    void setUp() {
        Organization org = orgRepository.save(Organization.builder().name("Test Org").build());
        
        manager = userRepository.save(User.builder()
                .email("manager@test.no").password("123").role(Role.MANAGER).organization(org).build());
        
        employee = userRepository.save(User.builder()
                .email("worker@test.no").password("123").role(Role.EMPLOYEE).organization(org).build());

        testDeviation = deviationRepository.save(Deviation.builder()
                .title("Old deviation")
                .status(DeviationStatus.OPEN)
                .organization(org)
                .build());
    }

    private void setMockUser(User user) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void employeeShouldBeAbleToReportDeviation() throws Exception {
        setMockUser(employee);
        Deviation newDev = Deviation.builder().title("Leak").description("Water on the floor").build();

        mockMvc.perform(post("/api/v1/deviations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(newDev)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OPEN")))
                .andExpect(jsonPath("$.title", is("Leak")));
    }

    @Test
    void employeeShouldBeForbiddenFromUpdatingStatus() throws Exception {
        setMockUser(employee);

        mockMvc.perform(patch("/api/v1/deviations/" + testDeviation.getId() + "/status")
                .param("status", "RESOLVED"))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerShouldBeAbleToResolveDeviation() throws Exception {
        setMockUser(manager);

        mockMvc.perform(patch("/api/v1/deviations/" + testDeviation.getId() + "/status")
                .param("status", "RESOLVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESOLVED")))
                .andExpect(jsonPath("$.resolvedAt", notNullValue()));
    }

    @Test
    void shouldOnlySeeDeviationsFromOwnOrg() throws Exception {
        // make another org and a deviation there
        Organization orgB = orgRepository.save(Organization.builder().name("Other Org").build());
        deviationRepository.save(Deviation.builder().title("Secret").organization(orgB).build());

        setMockUser(employee);

        mockMvc.perform(get("/api/v1/deviations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))) // should only see that from setUp()
                .andExpect(jsonPath("$[0].title", is("Old deviation")));
    }
}