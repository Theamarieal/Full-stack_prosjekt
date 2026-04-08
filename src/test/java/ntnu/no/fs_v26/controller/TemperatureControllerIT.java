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

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @Autowired
    private DeviationRepository deviationRepository;

    private Organization orgA;
    private Equipment fridge;
    private User userA;

    private void setMockUser(User user) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @BeforeEach
    void setUp() {
        orgA = orgRepository.save(Organization.builder().name("Everest Sushi").build());
        userA = userRepository.save(User.builder()
                .email("chef@sushi.no").password("123").role(Role.EMPLOYEE)
                .organization(orgA).active(true).build());

        // Fridge with allowed range 0–4 °C
        fridge = equipmentRepository.save(Equipment.builder()
                .name("FishFridge")
                .minTemp(0.0)
                .maxTemp(4.0)
                .organization(orgA)
                .build());
    }

    // ── 403 access control ────────────────────────────────────────────────

    @Test
    void getLogs_withoutAuthentication_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/temperature-logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    void postLog_withoutAuthentication_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/temperature-logs")
                        .param("equipmentId", fridge.getId().toString())
                        .param("value", "2.0"))
                .andExpect(status().isForbidden());
    }

    // ── Normal reading (no deviation) ─────────────────────────────────────

    @Test
    void logTemperature_withinRange_shouldNotFlagAsDeviation() throws Exception {
        setMockUser(userA);

        mockMvc.perform(post("/api/v1/temperature-logs")
                        .param("equipmentId", fridge.getId().toString())
                        .param("value", "2.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value", is(2.0)))
                .andExpect(jsonPath("$.deviation", is(false)));
    }

    @Test
    void logTemperature_exactlyAtMaxLimit_shouldNotFlagAsDeviation() throws Exception {
        setMockUser(userA);

        // 4.0 is the max — exactly at the boundary should be OK
        mockMvc.perform(post("/api/v1/temperature-logs")
                        .param("equipmentId", fridge.getId().toString())
                        .param("value", "4.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviation", is(false)));
    }

    // ── Deviation detection: too high ─────────────────────────────────────

    @Test
    void logTemperature_tooHigh_shouldFlagAsDeviation() throws Exception {
        setMockUser(userA);

        // 8.5 °C is above the max of 4.0 °C
        mockMvc.perform(post("/api/v1/temperature-logs")
                        .param("equipmentId", fridge.getId().toString())
                        .param("value", "8.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value", is(8.5)))
                .andExpect(jsonPath("$.deviation", is(true)));
    }

    @Test
    void logTemperature_tooHigh_shouldAutomaticallyCreateDeviationRecord() throws Exception {
        setMockUser(userA);

        long deviationsBefore = deviationRepository.count();

        mockMvc.perform(post("/api/v1/temperature-logs")
                        .param("equipmentId", fridge.getId().toString())
                        .param("value", "10.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviation", is(true)));

        // A deviation record should have been created automatically in the deviations table
        long deviationsAfter = deviationRepository.count();
        assertEquals(deviationsBefore + 1, deviationsAfter,
                "A deviation record should be created automatically for temperature deviations");

        List<Deviation> deviations = deviationRepository.findAllByOrganizationId(orgA.getId());
        Deviation created = deviations.get(deviations.size() - 1);
        assertEquals(DeviationStatus.OPEN, created.getStatus());
        assertEquals(DeviationModule.IK_MAT, created.getModule());
        assertTrue(created.getDescription().contains("10.0"));
    }

    // ── Deviation detection: too low ──────────────────────────────────────

    @Test
    void logTemperature_tooLow_shouldFlagAsDeviation() throws Exception {
        setMockUser(userA);

        // -5.0 °C is below the minimum of 0.0 °C
        mockMvc.perform(post("/api/v1/temperature-logs")
                        .param("equipmentId", fridge.getId().toString())
                        .param("value", "-5.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviation", is(true)));
    }

    @Test
    void logTemperature_tooLow_shouldAutomaticallyCreateDeviationRecord() throws Exception {
        setMockUser(userA);

        long deviationsBefore = deviationRepository.count();

        mockMvc.perform(post("/api/v1/temperature-logs")
                        .param("equipmentId", fridge.getId().toString())
                        .param("value", "-10.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviation", is(true)));

        assertEquals(deviationsBefore + 1, deviationRepository.count());
    }

    // ── Input validation ──────────────────────────────────────────────────

    @Test
    void logTemperature_extremelyHighValue_shouldReturn400() throws Exception {
        setMockUser(userA);

        // 200 °C is outside the allowed range of -50 to 100
        mockMvc.perform(post("/api/v1/temperature-logs")
                        .param("equipmentId", fridge.getId().toString())
                        .param("value", "200.0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logTemperature_extremelyLowValue_shouldReturn400() throws Exception {
        setMockUser(userA);

        mockMvc.perform(post("/api/v1/temperature-logs")
                        .param("equipmentId", fridge.getId().toString())
                        .param("value", "-200.0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logTemperature_equipmentFromOtherOrg_shouldReturn400() throws Exception {
        Organization orgB = orgRepository.save(Organization.builder().name("Burger King").build());
        Equipment otherFridge = equipmentRepository.save(Equipment.builder()
                .name("OtherFridge").minTemp(0).maxTemp(4).organization(orgB).build());

        setMockUser(userA);

        mockMvc.perform(post("/api/v1/temperature-logs")
                        .param("equipmentId", otherFridge.getId().toString())
                        .param("value", "2.0"))
                .andExpect(status().isBadRequest());
    }

    // ── Organization isolation ────────────────────────────────────────────

    @Test
    void getLogs_shouldOnlySeeOwnOrganizationLogs() throws Exception {
        Organization orgB = orgRepository.save(Organization.builder().name("Burger King").build());
        Equipment fridgeB = equipmentRepository.save(Equipment.builder()
                .name("MeatFreezer").minTemp(-25.0).maxTemp(-18.0).organization(orgB).build());

        logRepository.save(TemperatureLog.builder()
                .value(-20.0).timestamp(LocalDateTime.now())
                .equipment(fridgeB).isDeviation(false).build());

        setMockUser(userA);

        // OrgA has logged nothing yet — should see 0 results
        mockMvc.perform(get("/api/v1/temperature-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getLogs_afterLogging_shouldSeeOwnLogs() throws Exception {
        setMockUser(userA);

        // Log a normal temperature
        mockMvc.perform(post("/api/v1/temperature-logs")
                        .param("equipmentId", fridge.getId().toString())
                        .param("value", "3.0"))
                .andExpect(status().isOk());

        // Now fetch all logs — should see exactly 1
        mockMvc.perform(get("/api/v1/temperature-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].value", is(3.0)));
    }
}
