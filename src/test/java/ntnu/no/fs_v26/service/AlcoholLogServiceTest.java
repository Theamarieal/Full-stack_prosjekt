package ntnu.no.fs_v26.service;

import ntnu.no.fs_v26.controller.AlcoholLogRequest;
import ntnu.no.fs_v26.controller.AlcoholLogResponse;
import ntnu.no.fs_v26.model.AlcoholLog;
import ntnu.no.fs_v26.model.AlcoholLogType;
import ntnu.no.fs_v26.model.Deviation;
import ntnu.no.fs_v26.model.Organization;
import ntnu.no.fs_v26.model.Role;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.repository.AlcoholLogRepository;
import ntnu.no.fs_v26.repository.DeviationRepository;
import ntnu.no.fs_v26.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlcoholLogServiceTest {

    @Mock
    private AlcoholLogRepository alcoholLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DeviationRepository deviationRepository;

    @InjectMocks
    private AlcoholLogService alcoholLogService;

    private User employeeUser;
    private User managerUser;
    private Organization organization;

    @BeforeEach
    void setUp() {
        organization = Organization.builder()
                .id(1L)
                .name("Everest Sushi")
                .build();

        employeeUser = User.builder()
                .id(10L)
                .email("employee@test.no")
                .password("secret")
                .role(Role.EMPLOYEE)
                .organization(organization)
                .active(true)
                .build();

        managerUser = User.builder()
                .id(11L)
                .email("manager@test.no")
                .password("secret")
                .role(Role.MANAGER)
                .organization(organization)
                .active(true)
                .build();
    }

    @Test
    void createLog_shouldThrow_whenRecordedTimeMissing() {
        AlcoholLogRequest request = new AlcoholLogRequest();
        request.setType(AlcoholLogType.SERVING_START);
        request.setRecordedTime(null);

        when(userRepository.findByEmail(employeeUser.getEmail()))
                .thenReturn(Optional.of(employeeUser));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> alcoholLogService.createLog(request, employeeUser.getEmail())
        );

        assertEquals("Recorded time is required.", ex.getMessage());

        verify(alcoholLogRepository, never()).save(any(AlcoholLog.class));
        verify(alcoholLogRepository, never())
                .findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(anyLong(), any());
    }

    @Test
    void createLog_shouldSaveServingStart_withoutDeviation() {
        AlcoholLogRequest request = new AlcoholLogRequest();
        request.setType(AlcoholLogType.SERVING_START);
        request.setRecordedTime("10:00");

        when(userRepository.findByEmail(employeeUser.getEmail())).thenReturn(Optional.of(employeeUser));
        when(alcoholLogRepository.findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(
                organization.getId(), LocalDate.now()))
                .thenReturn(List.of());

        when(alcoholLogRepository.save(any(AlcoholLog.class))).thenAnswer(invocation -> {
            AlcoholLog log = invocation.getArgument(0);
            setId(log, 100L);
            return log;
        });

        AlcoholLogResponse response = alcoholLogService.createLog(request, employeeUser.getEmail());

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(AlcoholLogType.SERVING_START, response.getType());
        assertEquals(employeeUser.getEmail(), response.getRecordedBy());
        verify(deviationRepository, never()).save(any(Deviation.class));
    }

    @Test
    void createLog_shouldThrow_whenServingStartAlreadyRegistered() {
        AlcoholLog existingStart = existingLog(AlcoholLogType.SERVING_START, "10:00", employeeUser);

        AlcoholLogRequest request = new AlcoholLogRequest();
        request.setType(AlcoholLogType.SERVING_START);
        request.setRecordedTime("11:00");

        when(userRepository.findByEmail(employeeUser.getEmail())).thenReturn(Optional.of(employeeUser));
        when(alcoholLogRepository.findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(
                organization.getId(), LocalDate.now()))
                .thenReturn(List.of(existingStart));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> alcoholLogService.createLog(request, employeeUser.getEmail())
        );

        assertEquals("Serving start has already been registered for this shift.", ex.getMessage());
    }

    @Test
    void createLog_shouldThrow_whenServingEndBeforeServingStart() {
        AlcoholLogRequest request = new AlcoholLogRequest();
        request.setType(AlcoholLogType.SERVING_END);
        request.setRecordedTime("22:00");

        when(userRepository.findByEmail(employeeUser.getEmail())).thenReturn(Optional.of(employeeUser));
        when(alcoholLogRepository.findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(
                organization.getId(), LocalDate.now()))
                .thenReturn(List.of());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> alcoholLogService.createLog(request, employeeUser.getEmail())
        );

        assertEquals("Serving end cannot be registered before serving start.", ex.getMessage());
    }

    @Test
    void createLog_shouldThrow_whenAgeCheckBeforeServingStart() {
        AlcoholLogRequest request = validAgeCheckRequest();
        request.setRecordedTime("14:00");

        when(userRepository.findByEmail(employeeUser.getEmail())).thenReturn(Optional.of(employeeUser));
        when(alcoholLogRepository.findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(
                organization.getId(), LocalDate.now()))
                .thenReturn(List.of());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> alcoholLogService.createLog(request, employeeUser.getEmail())
        );

        assertEquals(
                "Age check can only be registered after serving has started and before serving has ended.",
                ex.getMessage()
        );
    }

    @Test
    void createLog_shouldThrow_whenIncidentWithoutNotes() {
        AlcoholLog existingStart = existingLog(AlcoholLogType.SERVING_START, "10:00", employeeUser);

        AlcoholLogRequest request = new AlcoholLogRequest();
        request.setType(AlcoholLogType.INCIDENT);
        request.setRecordedTime("14:00");
        request.setNotes(" ");

        when(userRepository.findByEmail(employeeUser.getEmail())).thenReturn(Optional.of(employeeUser));
        when(alcoholLogRepository.findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(
                organization.getId(), LocalDate.now()))
                .thenReturn(List.of(existingStart));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> alcoholLogService.createLog(request, employeeUser.getEmail())
        );

        assertEquals("Notes are required for incident logs.", ex.getMessage());
    }

    @Test
    void createLog_shouldThrow_whenAgeCheckMissingGuestAge() {
        AlcoholLog existingStart = existingLog(AlcoholLogType.SERVING_START, "10:00", employeeUser);

        AlcoholLogRequest request = validAgeCheckRequest();
        request.setGuestAge(null);

        when(userRepository.findByEmail(employeeUser.getEmail())).thenReturn(Optional.of(employeeUser));
        when(alcoholLogRepository.findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(
                organization.getId(), LocalDate.now()))
                .thenReturn(List.of(existingStart));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> alcoholLogService.createLog(request, employeeUser.getEmail())
        );

        assertEquals("Guest age is required for age checks.", ex.getMessage());
    }

    @Test
    void createLog_shouldThrow_whenNotesProvidedForNormalAgeCheck() {
        AlcoholLog existingStart = existingLog(AlcoholLogType.SERVING_START, "10:00", employeeUser);

        AlcoholLogRequest request = validAgeCheckRequest();
        request.setNotes("This should not be allowed");

        when(userRepository.findByEmail(employeeUser.getEmail())).thenReturn(Optional.of(employeeUser));
        when(alcoholLogRepository.findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(
                organization.getId(), LocalDate.now()))
                .thenReturn(List.of(existingStart));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> alcoholLogService.createLog(request, employeeUser.getEmail())
        );

        assertEquals(
                "Notes are only allowed for incidents or denied age checks that require an explanation.",
                ex.getMessage()
        );
    }

    @Test
    void createLog_shouldCreateDeviation_whenIdNotChecked() {
        AlcoholLog existingStart = existingLog(AlcoholLogType.SERVING_START, "10:00", employeeUser);

        AlcoholLogRequest request = validAgeCheckRequest();
        request.setIdChecked(false);

        when(userRepository.findByEmail(employeeUser.getEmail())).thenReturn(Optional.of(employeeUser));
        when(alcoholLogRepository.findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(
                organization.getId(), LocalDate.now()))
                .thenReturn(List.of(existingStart));

        when(alcoholLogRepository.save(any(AlcoholLog.class))).thenAnswer(invocation -> {
            AlcoholLog log = invocation.getArgument(0);
            setId(log, 101L);
            return log;
        });

        alcoholLogService.createLog(request, employeeUser.getEmail());

        ArgumentCaptor<Deviation> captor = ArgumentCaptor.forClass(Deviation.class);
        verify(deviationRepository).save(captor.capture());

        Deviation deviation = captor.getValue();
        assertEquals("Alcohol compliance deviation: ID not checked", deviation.getTitle());
        assertEquals(employeeUser, deviation.getReportedBy());
        assertEquals(organization, deviation.getOrganization());
    }

    @Test
    void createLog_shouldCreateDeviation_whenServingUnder18() {
        AlcoholLog existingStart = existingLog(AlcoholLogType.SERVING_START, "10:00", employeeUser);

        AlcoholLogRequest request = validAgeCheckRequest();
        request.setGuestAge(17);
        request.setAlcoholPercentage(4.7);
        request.setServiceDenied(false);

        when(userRepository.findByEmail(employeeUser.getEmail())).thenReturn(Optional.of(employeeUser));
        when(alcoholLogRepository.findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(
                organization.getId(), LocalDate.now()))
                .thenReturn(List.of(existingStart));

        when(alcoholLogRepository.save(any(AlcoholLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        alcoholLogService.createLog(request, employeeUser.getEmail());

        ArgumentCaptor<Deviation> captor = ArgumentCaptor.forClass(Deviation.class);
        verify(deviationRepository).save(captor.capture());

        assertEquals("Alcohol compliance deviation: Service to guest under 18",
                captor.getValue().getTitle());
    }

    @Test
    void createLog_shouldCreateDeviation_whenStrongAlcoholServedToGuestUnder20() {
        AlcoholLog existingStart = existingLog(AlcoholLogType.SERVING_START, "10:00", employeeUser);

        AlcoholLogRequest request = validAgeCheckRequest();
        request.setGuestAge(19);
        request.setAlcoholPercentage(40.0);
        request.setServiceDenied(false);

        when(userRepository.findByEmail(employeeUser.getEmail())).thenReturn(Optional.of(employeeUser));
        when(alcoholLogRepository.findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(
                organization.getId(), LocalDate.now()))
                .thenReturn(List.of(existingStart));

        when(alcoholLogRepository.save(any(AlcoholLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        alcoholLogService.createLog(request, employeeUser.getEmail());

        ArgumentCaptor<Deviation> captor = ArgumentCaptor.forClass(Deviation.class);
        verify(deviationRepository).save(captor.capture());

        assertEquals("Alcohol compliance deviation: Strong alcohol served to guest under 20",
                captor.getValue().getTitle());
    }

    @Test
    void createLog_shouldCreateDeviation_whenServiceDeniedThoughGuestIsOldEnough() {
        AlcoholLog existingStart = existingLog(AlcoholLogType.SERVING_START, "10:00", employeeUser);

        AlcoholLogRequest request = validAgeCheckRequest();
        request.setGuestAge(20);
        request.setAlcoholPercentage(4.7);
        request.setServiceDenied(true);
        request.setNotes("Guest was visibly intoxicated");

        when(userRepository.findByEmail(employeeUser.getEmail())).thenReturn(Optional.of(employeeUser));
        when(alcoholLogRepository.findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(
                organization.getId(), LocalDate.now()))
                .thenReturn(List.of(existingStart));

        when(alcoholLogRepository.save(any(AlcoholLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AlcoholLogResponse response = alcoholLogService.createLog(request, employeeUser.getEmail());

        assertEquals("Guest was visibly intoxicated", response.getNotes());

        ArgumentCaptor<Deviation> captor = ArgumentCaptor.forClass(Deviation.class);
        verify(deviationRepository).save(captor.capture());

        assertEquals("Alcohol compliance deviation: Service denied despite legal age",
                captor.getValue().getTitle());
    }

    @Test
    void createLog_shouldThrow_whenDeniedOldEnoughGuestHasNoReason() {
        AlcoholLog existingStart = existingLog(AlcoholLogType.SERVING_START, "10:00", employeeUser);

        AlcoholLogRequest request = validAgeCheckRequest();
        request.setGuestAge(20);
        request.setAlcoholPercentage(4.7);
        request.setServiceDenied(true);
        request.setNotes(" ");

        when(userRepository.findByEmail(employeeUser.getEmail())).thenReturn(Optional.of(employeeUser));
        when(alcoholLogRepository.findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(
                organization.getId(), LocalDate.now()))
                .thenReturn(List.of(existingStart));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> alcoholLogService.createLog(request, employeeUser.getEmail())
        );

        assertEquals(
                "A reason is required when service is denied despite the guest being old enough.",
                ex.getMessage()
        );
    }

    @Test
    void createLog_shouldCreateDeviation_whenServingStartOutsideAllowedWindow() {
        AlcoholLogRequest request = new AlcoholLogRequest();
        request.setType(AlcoholLogType.SERVING_START);
        request.setRecordedTime("03:00");

        when(userRepository.findByEmail(employeeUser.getEmail())).thenReturn(Optional.of(employeeUser));
        when(alcoholLogRepository.findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(
                organization.getId(), LocalDate.now()))
                .thenReturn(List.of());

        when(alcoholLogRepository.save(any(AlcoholLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        alcoholLogService.createLog(request, employeeUser.getEmail());

        ArgumentCaptor<Deviation> captor = ArgumentCaptor.forClass(Deviation.class);
        verify(deviationRepository).save(captor.capture());

        assertEquals("Alcohol compliance deviation: Serving outside allowed time window",
                captor.getValue().getTitle());
    }

    @Test
    void getCurrentDayLogs_shouldReturnMappedResponses() {
        AlcoholLog log = existingLog(AlcoholLogType.INCIDENT, "14:30", employeeUser);
        log.setNotes("Incident happened");

        when(userRepository.findByEmail(employeeUser.getEmail())).thenReturn(Optional.of(employeeUser));
        when(alcoholLogRepository.findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(
                organization.getId(), LocalDate.now()))
                .thenReturn(List.of(log));

        List<AlcoholLogResponse> responses = alcoholLogService.getCurrentDayLogs(employeeUser.getEmail());

        assertEquals(1, responses.size());
        assertEquals(employeeUser.getEmail(), responses.get(0).getRecordedBy());
        assertEquals("Incident happened", responses.get(0).getNotes());
    }

    @Test
    void getLogsByDate_shouldThrowForEmployee() {
        when(userRepository.findByEmail(employeeUser.getEmail())).thenReturn(Optional.of(employeeUser));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> alcoholLogService.getLogsByDate(employeeUser.getEmail(), LocalDate.now().minusDays(1))
        );

        assertEquals("You are not allowed to search historical alcohol logs.", ex.getMessage());
    }

    @Test
    void getLogsByDate_shouldReturnLogsForManager() {
        LocalDate date = LocalDate.now().minusDays(1);
        AlcoholLog log = existingLog(AlcoholLogType.SERVING_END, "22:00", managerUser);

        when(userRepository.findByEmail(managerUser.getEmail())).thenReturn(Optional.of(managerUser));
        when(alcoholLogRepository.findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(
                organization.getId(), date))
                .thenReturn(List.of(log));

        List<AlcoholLogResponse> responses = alcoholLogService.getLogsByDate(managerUser.getEmail(), date);

        assertEquals(1, responses.size());
        assertEquals(AlcoholLogType.SERVING_END, responses.get(0).getType());
    }

    @Test
    void hasLogsForToday_shouldReturnTrue_whenLogsExist() {
        AlcoholLog log = existingLog(AlcoholLogType.SERVING_START, "10:00", employeeUser);

        when(userRepository.findByEmail(employeeUser.getEmail())).thenReturn(Optional.of(employeeUser));
        when(alcoholLogRepository.findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(
                organization.getId(), LocalDate.now()))
                .thenReturn(List.of(log));

        assertTrue(alcoholLogService.hasLogsForToday(employeeUser.getEmail()));
    }

    @Test
    void hasLogsForToday_shouldReturnFalse_whenNoLogsExist() {
        when(userRepository.findByEmail(employeeUser.getEmail())).thenReturn(Optional.of(employeeUser));
        when(alcoholLogRepository.findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(
                organization.getId(), LocalDate.now()))
                .thenReturn(List.of());

        assertFalse(alcoholLogService.hasLogsForToday(employeeUser.getEmail()));
    }

    @Test
    void createLog_shouldThrow_whenAuthenticatedUserNotFound() {
        AlcoholLogRequest request = new AlcoholLogRequest();
        request.setType(AlcoholLogType.SERVING_START);
        request.setRecordedTime("10:00");

        when(userRepository.findByEmail("missing@test.no")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> alcoholLogService.createLog(request, "missing@test.no")
        );

        assertEquals("Authenticated user was not found.", ex.getMessage());
    }

    private AlcoholLogRequest validAgeCheckRequest() {
        AlcoholLogRequest request = new AlcoholLogRequest();
        request.setType(AlcoholLogType.AGE_CHECK);
        request.setRecordedTime("14:00");
        request.setGuestAge(19);
        request.setAlcoholPercentage(4.7);
        request.setIdChecked(true);
        request.setServiceDenied(false);
        return request;
    }

    private AlcoholLog existingLog(AlcoholLogType type, String time, User user) {
        AlcoholLog log = new AlcoholLog();
        setId(log, 999L);
        log.setType(type);
        log.setRecordedAt(LocalDateTime.of(LocalDate.now(), java.time.LocalTime.parse(time)));
        log.setShiftDate(LocalDate.now());
        log.setRecordedBy(user);
        log.setOrganization(user.getOrganization());
        return log;
    }

    private void setId(AlcoholLog log, Long id) {
        try {
            var field = AlcoholLog.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(log, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set AlcoholLog id in test", e);
        }
    }
}