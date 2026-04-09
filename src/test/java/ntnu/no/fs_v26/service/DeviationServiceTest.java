package ntnu.no.fs_v26.service;

import ntnu.no.fs_v26.controller.DeviationRequest;
import ntnu.no.fs_v26.model.*;
import ntnu.no.fs_v26.repository.DeviationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviationServiceTest {

    @Mock
    private DeviationRepository repository;

    @InjectMocks
    private DeviationService deviationService;

    private Organization org;
    private Organization otherOrg;
    private User manager;
    private User employee;

    @BeforeEach
    void setUp() {
        org = Organization.builder().name("Sushi Place").build();
        setId(org, 1L);

        otherOrg = Organization.builder().name("Burger Place").build();
        setId(otherOrg, 2L);

        manager = User.builder()
                .email("manager@sushi.no")
                .role(Role.MANAGER)
                .organization(org)
                .build();

        employee = User.builder()
                .email("employee@sushi.no")
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

    // ── reportDeviation ───────────────────────────────────────────────────

    @Test
    void reportDeviation_shouldCreateWithStatusOpen() {
        DeviationRequest request = new DeviationRequest();
        request.setTitle("Water leak");
        request.setDescription("Leak under the sink");
        request.setModule(DeviationModule.IK_MAT);

        when(repository.save(any(Deviation.class))).thenAnswer(inv -> inv.getArgument(0));

        Deviation result = deviationService.reportDeviation(request, employee);

        assertEquals("Water leak", result.getTitle());
        assertEquals(DeviationStatus.OPEN, result.getStatus());
        assertEquals(employee, result.getReportedBy());
        assertEquals(org, result.getOrganization());
        assertNotNull(result.getCreatedAt());
        verify(repository).save(any(Deviation.class));
    }

    @Test
    void reportDeviation_organizationAlwaysTakenFromUser() {
        DeviationRequest request = new DeviationRequest();
        request.setTitle("Test deviation");

        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Deviation result = deviationService.reportDeviation(request, employee);

        // Organization must come from user, never from request body
        assertEquals(org, result.getOrganization());
    }

    // ── updateStatus ──────────────────────────────────────────────────────

    @Test
    void updateStatus_toInProgress_shouldSucceed() {
        Deviation deviation = Deviation.builder()
                .title("Old leak")
                .status(DeviationStatus.OPEN)
                .organization(org)
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(deviation));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Deviation result = deviationService.updateStatus(1L, DeviationStatus.IN_PROGRESS, manager);

        assertEquals(DeviationStatus.IN_PROGRESS, result.getStatus());
        assertNull(result.getResolvedAt()); // only set on RESOLVED
    }

    @Test
    void updateStatus_toResolved_shouldSetResolvedAt() {
        Deviation deviation = Deviation.builder()
                .title("Fixed leak")
                .status(DeviationStatus.IN_PROGRESS)
                .organization(org)
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(deviation));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Deviation result = deviationService.updateStatus(1L, DeviationStatus.RESOLVED, manager);

        assertEquals(DeviationStatus.RESOLVED, result.getStatus());
        assertNotNull(result.getResolvedAt());
    }

    @Test
    void updateStatus_backFromResolved_shouldClearResolvedAt() {
        Deviation deviation = Deviation.builder()
                .title("Reopened issue")
                .status(DeviationStatus.RESOLVED)
                .resolvedAt(LocalDateTime.now())
                .organization(org)
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(deviation));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Deviation result = deviationService.updateStatus(1L, DeviationStatus.IN_PROGRESS, manager);

        assertEquals(DeviationStatus.IN_PROGRESS, result.getStatus());
        assertNull(result.getResolvedAt()); // should be cleared
    }

    @Test
    void updateStatus_deviationNotFound_shouldThrowIllegalArgument() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> deviationService.updateStatus(99L, DeviationStatus.RESOLVED, manager));
    }

    @Test
    void updateStatus_deviationFromOtherOrg_shouldThrowAccessDenied() {
        Deviation otherDeviation = Deviation.builder()
                .title("Not mine")
                .status(DeviationStatus.OPEN)
                .organization(otherOrg)
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findById(5L)).thenReturn(Optional.of(otherDeviation));

        assertThrows(AccessDeniedException.class,
                () -> deviationService.updateStatus(5L, DeviationStatus.RESOLVED, manager));
        verify(repository, never()).save(any());
    }
}
