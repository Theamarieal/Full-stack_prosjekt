package ntnu.no.fs_v26.service;

import ntnu.no.fs_v26.model.*;
import ntnu.no.fs_v26.repository.DeviationRepository;
import ntnu.no.fs_v26.repository.EquipmentRepository;
import ntnu.no.fs_v26.repository.TemperatureLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemperatureServiceTest {

    @Mock
    private TemperatureLogRepository logRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private DeviationRepository deviationRepository;

    @InjectMocks
    private TemperatureService temperatureService;

    private Organization org;
    private Organization otherOrg;
    private Equipment fridge;
    private User employee;

    @BeforeEach
    void setUp() {
        org = Organization.builder().name("Sushi Place").build();
        setId(org, 1L);

        otherOrg = Organization.builder().name("Other Place").build();
        setId(otherOrg, 2L);

        fridge = Equipment.builder()
                .name("Kitchen Fridge")
                .minTemp(0.0)
                .maxTemp(4.0)
                .organization(org)
                .build();
        setId(fridge, 10L);

        employee = User.builder()
                .email("chef@sushi.no")
                .role(Role.EMPLOYEE)
                .organization(org)
                .build();
    }

    private <T> T setId(T obj, Long id) {
        try {
            var field = obj.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(obj, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return obj;
    }

    // ── Normal readings ───────────────────────────────────────────────────

    @Test
    void logTemperature_withinRange_shouldNotFlagDeviation() {
        when(equipmentRepository.findById(10L)).thenReturn(Optional.of(fridge));
        when(logRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TemperatureLog result = temperatureService.logTemperature(10L, 2.0, employee);

        assertFalse(result.isDeviation());
        verify(deviationRepository, never()).save(any()); // no deviation created
    }

    @Test
    void logTemperature_exactlyAtMaxBoundary_shouldNotFlagDeviation() {
        when(equipmentRepository.findById(10L)).thenReturn(Optional.of(fridge));
        when(logRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TemperatureLog result = temperatureService.logTemperature(10L, 4.0, employee);

        assertFalse(result.isDeviation());
    }

    @Test
    void logTemperature_exactlyAtMinBoundary_shouldNotFlagDeviation() {
        when(equipmentRepository.findById(10L)).thenReturn(Optional.of(fridge));
        when(logRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TemperatureLog result = temperatureService.logTemperature(10L, 0.0, employee);

        assertFalse(result.isDeviation());
    }

    // ── Deviation detection ───────────────────────────────────────────────

    @Test
    void logTemperature_tooHigh_shouldFlagDeviationAndCreateDeviationRecord() {
        when(equipmentRepository.findById(10L)).thenReturn(Optional.of(fridge));
        when(logRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(deviationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TemperatureLog result = temperatureService.logTemperature(10L, 8.5, employee);

        assertTrue(result.isDeviation());
        verify(deviationRepository, times(1)).save(any(Deviation.class));
    }

    @Test
    void logTemperature_tooLow_shouldFlagDeviation() {
        when(equipmentRepository.findById(10L)).thenReturn(Optional.of(fridge));
        when(logRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(deviationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TemperatureLog result = temperatureService.logTemperature(10L, -5.0, employee);

        assertTrue(result.isDeviation());
        verify(deviationRepository, times(1)).save(any(Deviation.class));
    }

    @Test
    void logTemperature_deviation_shouldCreateDeviationWithCorrectFields() {
        when(equipmentRepository.findById(10L)).thenReturn(Optional.of(fridge));
        when(logRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<Deviation> deviationCaptor = ArgumentCaptor.forClass(Deviation.class);
        when(deviationRepository.save(deviationCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        temperatureService.logTemperature(10L, 10.0, employee);

        Deviation created = deviationCaptor.getValue();
        assertEquals(DeviationStatus.OPEN, created.getStatus());
        assertEquals(DeviationModule.IK_MAT, created.getModule());
        assertEquals(org, created.getOrganization());
        assertTrue(created.getDescription().contains("10.0"));
        assertTrue(created.getDescription().contains("Kitchen Fridge"));
    }

    @Test
    void logTemperature_normal_shouldNotCreateDeviationRecord() {
        when(equipmentRepository.findById(10L)).thenReturn(Optional.of(fridge));
        when(logRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        temperatureService.logTemperature(10L, 2.0, employee);

        verify(deviationRepository, never()).save(any());
    }

    // ── Input validation ──────────────────────────────────────────────────

    @Test
    void logTemperature_aboveAbsoluteMax_shouldThrowRuntimeException() {
        when(equipmentRepository.findById(10L)).thenReturn(Optional.of(fridge));

        assertThrows(RuntimeException.class,
                () -> temperatureService.logTemperature(10L, 200.0, employee));
        verify(logRepository, never()).save(any());
    }

    @Test
    void logTemperature_belowAbsoluteMin_shouldThrowRuntimeException() {
        when(equipmentRepository.findById(10L)).thenReturn(Optional.of(fridge));

        assertThrows(RuntimeException.class,
                () -> temperatureService.logTemperature(10L, -200.0, employee));
        verify(logRepository, never()).save(any());
    }

    @Test
    void logTemperature_equipmentNotFound_shouldThrowRuntimeException() {
        when(equipmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> temperatureService.logTemperature(99L, 2.0, employee));
    }

    @Test
    void logTemperature_equipmentFromOtherOrg_shouldThrowRuntimeException() {
        Equipment otherFridge = Equipment.builder()
                .name("Other Fridge")
                .minTemp(0.0).maxTemp(4.0)
                .organization(otherOrg)
                .build();
        setId(otherFridge, 20L);

        when(equipmentRepository.findById(20L)).thenReturn(Optional.of(otherFridge));

        assertThrows(RuntimeException.class,
                () -> temperatureService.logTemperature(20L, 2.0, employee));
        verify(logRepository, never()).save(any());
    }

    // ── Metadata on saved log ─────────────────────────────────────────────

    @Test
    void logTemperature_shouldSetTimestampAndLoggedBy() {
        when(equipmentRepository.findById(10L)).thenReturn(Optional.of(fridge));
        when(logRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TemperatureLog result = temperatureService.logTemperature(10L, 3.0, employee);

        assertEquals(3.0, result.getValue());
        assertEquals(employee, result.getLoggedBy());
        assertEquals(fridge, result.getEquipment());
        assertNotNull(result.getTimestamp());
    }
}
