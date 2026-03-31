package ntnu.no.fs_v26.controller;

import ntnu.no.fs_v26.model.*;
import ntnu.no.fs_v26.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class TemperatureControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private OrganizationRepository orgRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TemperatureLogRepository logRepository;

    private Organization orgA;
    private Equipment fridge;
    private User userA;

    @BeforeEach
    void setUp() {
        // 1. make an organization and user
        orgA = orgRepository.save(Organization.builder().name("Everest Sushi").build());
        userA = userRepository.save(User.builder()
                .email("chef@sushi.no").password("123").role(Role.EMPLOYEE).organization(orgA).build());

        // 2. make equipment with temperaturelimits (0 to 4 degrees)
        fridge = equipmentRepository.save(Equipment.builder()
                .name("FishFridge")
                .minTemp(0.0)
                .maxTemp(4.0)
                .organization(orgA)
                .build());
    }

    private void setMockUser(User user) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void shouldDetectDeviationWhenTempIsTooHigh() throws Exception {
        setMockUser(userA);

        // we log 8.5 degrees (limit is 4.0)
        mockMvc.perform(post("/api/v1/temperature-logs")
                .param("equipmentId", fridge.getId().toString())
                .param("value", "8.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value", is(8.5)))
                .andExpect(jsonPath("$.deviation", is(true))); // automatic deviation!
    }

    @Test
    void shouldNotDetectDeviationWhenTempIsNormal() throws Exception {
        setMockUser(userA);

        // we log 2.0 degrees (within 0-4)
        mockMvc.perform(post("/api/v1/temperature-logs")
                .param("equipmentId", fridge.getId().toString())
                .param("value", "2.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviation", is(false))); // no deviation
    }

    @Test
    void shouldOnlySeeLogsFromOwnOrganization() throws Exception {
        // make anothen organization with its own log
        Organization orgB = orgRepository.save(Organization.builder().name("Burger King").build());
        Equipment fridgeB = equipmentRepository.save(Equipment.builder()
                .name("meat-freezer").minTemp(-25.0).maxTemp(-18.0).organization(orgB).build());
        
        logRepository.save(TemperatureLog.builder()
                .value(-20.0).equipment(fridgeB).isDeviation(false).build());

        // log in as a user from Org A
        setMockUser(userA);

        // check that we see 0 logs (since we havent logged anything for Org A yet)
        mockMvc.perform(get("/api/v1/temperature-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}