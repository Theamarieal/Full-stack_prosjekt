package ntnu.no.fs_v26.service;

import ntnu.no.fs_v26.controller.AdminCreateOrganizationRequest;
import ntnu.no.fs_v26.controller.AdminCreateUserRequest;
import ntnu.no.fs_v26.controller.AdminToggleActiveRequest;
import ntnu.no.fs_v26.controller.AdminUpdateRoleRequest;
import ntnu.no.fs_v26.exception.ResourceConflictException;
import ntnu.no.fs_v26.exception.ResourceNotFoundException;
import ntnu.no.fs_v26.model.Organization;
import ntnu.no.fs_v26.model.Role;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.repository.AlcoholLogRepository;
import ntnu.no.fs_v26.repository.ChecklistItemRepository;
import ntnu.no.fs_v26.repository.DeviationRepository;
import ntnu.no.fs_v26.repository.OrganizationRepository;
import ntnu.no.fs_v26.repository.TemperatureLogRepository;
import ntnu.no.fs_v26.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ChecklistItemRepository checklistItemRepository;

    @Mock
    private TemperatureLogRepository temperatureLogRepository;

    @Mock
    private DeviationRepository deviationRepository;

    @Mock
    private AlcoholLogRepository alcoholLogRepository;

    @InjectMocks
    private AdminService adminService;

    private Organization organization;
    private User adminUser;
    private User employeeUser;

    @BeforeEach
    void setUp() {
        organization = Organization.builder()
                .id(1L)
                .name("Everest Sushi")
                .build();

        adminUser = User.builder()
                .id(10L)
                .email("admin@test.no")
                .password("encoded")
                .role(Role.ADMIN)
                .organization(organization)
                .active(true)
                .build();

        employeeUser = User.builder()
                .id(11L)
                .email("employee@test.no")
                .password("encoded")
                .role(Role.EMPLOYEE)
                .organization(organization)
                .active(true)
                .build();
    }

    @Test
    void getAllUsers_shouldReturnMappedUsersForOrganization() {
        User managerUser = User.builder()
                .id(12L)
                .email("manager@test.no")
                .password("encoded")
                .role(Role.MANAGER)
                .organization(organization)
                .active(false)
                .build();

        when(userRepository.findByOrganization(organization))
                .thenReturn(List.of(adminUser, managerUser));

        List<Map<String, Object>> result = adminService.getAllUsers(adminUser);

        assertEquals(2, result.size());

        assertEquals(10L, result.get(0).get("id"));
        assertEquals("admin@test.no", result.get(0).get("email"));
        assertEquals("ADMIN", result.get(0).get("role"));
        assertEquals(true, result.get(0).get("active"));

        assertEquals(12L, result.get(1).get("id"));
        assertEquals("manager@test.no", result.get(1).get("email"));
        assertEquals("MANAGER", result.get(1).get("role"));
        assertEquals(false, result.get(1).get("active"));
    }

    @Test
    void createUser_shouldCreateUserInCurrentUsersOrganization() {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setEmail("newuser@test.no");
        request.setPassword("Secret123");
        request.setRole(Role.MANAGER);

        when(userRepository.findByEmail("newuser@test.no")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Secret123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return User.builder()
                    .id(100L)
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .role(user.getRole())
                    .organization(user.getOrganization())
                    .active(user.isActive())
                    .build();
        });

        Map<String, Object> result = adminService.createUser(request, adminUser);

        assertEquals(100L, result.get("id"));
        assertEquals("newuser@test.no", result.get("email"));
        assertEquals("MANAGER", result.get("role"));
        assertEquals(true, result.get("active"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();
        assertEquals("newuser@test.no", savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals(Role.MANAGER, savedUser.getRole());
        assertEquals(organization, savedUser.getOrganization());
        assertTrue(savedUser.isActive());
    }

    @Test
    void createUser_shouldThrowWhenEmailAlreadyInUse() {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setEmail("employee@test.no");
        request.setPassword("Secret123");
        request.setRole(Role.EMPLOYEE);

        when(userRepository.findByEmail("employee@test.no")).thenReturn(Optional.of(employeeUser));

        ResourceConflictException ex = assertThrows(
                ResourceConflictException.class,
                () -> adminService.createUser(request, adminUser)
        );

        assertEquals("E-mail already in use: employee@test.no", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void createOrganization_shouldCreateOrganizationAndAdminUser() {
        AdminCreateOrganizationRequest request = new AdminCreateOrganizationRequest();
        request.setOrganizationName("New Restaurant");
        request.setAdminEmail("newadmin@test.no");
        request.setAdminPassword("Secret123");

        when(userRepository.findByEmail("newadmin@test.no")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Secret123")).thenReturn("encoded-password");

        when(organizationRepository.save(any(Organization.class))).thenAnswer(invocation -> {
            Organization org = invocation.getArgument(0);
            return Organization.builder()
                    .id(200L)
                    .name(org.getName())
                    .build();
        });

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return User.builder()
                    .id(201L)
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .role(user.getRole())
                    .organization(user.getOrganization())
                    .active(user.isActive())
                    .build();
        });

        Map<String, Object> result = adminService.createOrganization(request);

        assertEquals(200L, result.get("organizationId"));
        assertEquals("New Restaurant", result.get("organizationName"));
        assertEquals("newadmin@test.no", result.get("adminEmail"));
        assertEquals("ADMIN", result.get("adminRole"));

        ArgumentCaptor<Organization> orgCaptor = ArgumentCaptor.forClass(Organization.class);
        verify(organizationRepository).save(orgCaptor.capture());
        assertEquals("New Restaurant", orgCaptor.getValue().getName());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User admin = userCaptor.getValue();
        assertEquals("newadmin@test.no", admin.getEmail());
        assertEquals("encoded-password", admin.getPassword());
        assertEquals(Role.ADMIN, admin.getRole());
        assertEquals(200L, admin.getOrganization().getId());
        assertTrue(admin.isActive());
    }

    @Test
    void createOrganization_shouldThrowWhenAdminEmailAlreadyExists() {
        AdminCreateOrganizationRequest request = new AdminCreateOrganizationRequest();
        request.setOrganizationName("New Restaurant");
        request.setAdminEmail("admin@test.no");
        request.setAdminPassword("Secret123");

        when(userRepository.findByEmail("admin@test.no")).thenReturn(Optional.of(adminUser));

        ResourceConflictException ex = assertThrows(
                ResourceConflictException.class,
                () -> adminService.createOrganization(request)
        );

        assertEquals("E-mail already in use: admin@test.no", ex.getMessage());
        verify(organizationRepository, never()).save(any(Organization.class));
    }

    @Test
    void updateUserRole_shouldUpdateRoleWhenUserExists() {
        AdminUpdateRoleRequest request = new AdminUpdateRoleRequest();
        request.setRole(Role.MANAGER);

        when(userRepository.findById(11L)).thenReturn(Optional.of(employeeUser));
        when(userRepository.save(employeeUser)).thenReturn(employeeUser);

        Map<String, Object> result = adminService.updateUserRole(11L, request);

        assertEquals("MANAGER", result.get("role"));
        assertEquals("employee@test.no", result.get("email"));
        assertEquals(true, result.get("active"));
        assertEquals(Role.MANAGER, employeeUser.getRole());
    }

    @Test
    void updateUserRole_shouldThrowWhenUserNotFound() {
        AdminUpdateRoleRequest request = new AdminUpdateRoleRequest();
        request.setRole(Role.MANAGER);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> adminService.updateUserRole(999L, request)
        );

        assertEquals("User not found: 999", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserRole_shouldThrowWhenChangingLastActiveAdmin() {
        AdminUpdateRoleRequest request = new AdminUpdateRoleRequest();
        request.setRole(Role.MANAGER);

        when(userRepository.findById(10L)).thenReturn(Optional.of(adminUser));
        when(userRepository.findByOrganization(organization)).thenReturn(List.of(adminUser));

        ResourceConflictException ex = assertThrows(
                ResourceConflictException.class,
                () -> adminService.updateUserRole(10L, request)
        );

        assertEquals("Cannot change role of the last admin in the organization.", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserRole_shouldAllowChangingAdminRoleWhenAnotherActiveAdminExists() {
        User secondAdmin = User.builder()
                .id(12L)
                .email("admin2@test.no")
                .password("encoded")
                .role(Role.ADMIN)
                .organization(organization)
                .active(true)
                .build();

        AdminUpdateRoleRequest request = new AdminUpdateRoleRequest();
        request.setRole(Role.MANAGER);

        when(userRepository.findById(10L)).thenReturn(Optional.of(adminUser));
        when(userRepository.findByOrganization(organization)).thenReturn(List.of(adminUser, secondAdmin));
        when(userRepository.save(adminUser)).thenReturn(adminUser);

        Map<String, Object> result = adminService.updateUserRole(10L, request);

        assertEquals("MANAGER", result.get("role"));
        assertEquals(Role.MANAGER, adminUser.getRole());
    }

    @Test
    void toggleUserActive_shouldUpdateActiveStatus() {
        AdminToggleActiveRequest request = new AdminToggleActiveRequest();
        request.setActive(false);

        when(userRepository.findById(11L)).thenReturn(Optional.of(employeeUser));
        when(userRepository.save(employeeUser)).thenReturn(employeeUser);

        Map<String, Object> result = adminService.toggleUserActive(11L, request);

        assertEquals(false, result.get("active"));
        assertEquals("employee@test.no", result.get("email"));
        assertFalse(employeeUser.isActive());
    }

    @Test
    void toggleUserActive_shouldThrowWhenUserNotFound() {
        AdminToggleActiveRequest request = new AdminToggleActiveRequest();
        request.setActive(false);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> adminService.toggleUserActive(999L, request)
        );

        assertEquals("User not found: 999", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_shouldDeleteWhenNoReferencesExist() {
        when(userRepository.findById(11L)).thenReturn(Optional.of(employeeUser));
        when(checklistItemRepository.existsByCompletedBy(employeeUser)).thenReturn(false);
        when(temperatureLogRepository.existsByLoggedBy(employeeUser)).thenReturn(false);
        when(deviationRepository.existsByReportedBy(employeeUser)).thenReturn(false);
        when(alcoholLogRepository.existsByRecordedBy(employeeUser)).thenReturn(false);

        adminService.deleteUser(11L);

        verify(userRepository).delete(employeeUser);
    }

    @Test
    void deleteUser_shouldThrowWhenChecklistReferenceExists() {
        when(userRepository.findById(11L)).thenReturn(Optional.of(employeeUser));
        when(checklistItemRepository.existsByCompletedBy(employeeUser)).thenReturn(true);

        ResourceConflictException ex = assertThrows(
                ResourceConflictException.class,
                () -> adminService.deleteUser(11L)
        );

        assertEquals(
                "User cannot be deleted because they have existing logs or deviations. Deactivate the user instead.",
                ex.getMessage()
        );

        verify(userRepository, never()).delete(any(User.class));
        verify(temperatureLogRepository, never()).existsByLoggedBy(any(User.class));
    }

    @Test
    void deleteUser_shouldThrowWhenTemperatureReferenceExists() {
        when(userRepository.findById(11L)).thenReturn(Optional.of(employeeUser));
        when(checklistItemRepository.existsByCompletedBy(employeeUser)).thenReturn(false);
        when(temperatureLogRepository.existsByLoggedBy(employeeUser)).thenReturn(true);

        ResourceConflictException ex = assertThrows(
                ResourceConflictException.class,
                () -> adminService.deleteUser(11L)
        );

        assertEquals(
                "User cannot be deleted because they have existing logs or deviations. Deactivate the user instead.",
                ex.getMessage()
        );

        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUser_shouldThrowWhenDeviationReferenceExists() {
        when(userRepository.findById(11L)).thenReturn(Optional.of(employeeUser));
        when(checklistItemRepository.existsByCompletedBy(employeeUser)).thenReturn(false);
        when(temperatureLogRepository.existsByLoggedBy(employeeUser)).thenReturn(false);
        when(deviationRepository.existsByReportedBy(employeeUser)).thenReturn(true);

        ResourceConflictException ex = assertThrows(
                ResourceConflictException.class,
                () -> adminService.deleteUser(11L)
        );

        assertEquals(
                "User cannot be deleted because they have existing logs or deviations. Deactivate the user instead.",
                ex.getMessage()
        );

        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUser_shouldThrowWhenAlcoholLogReferenceExists() {
        when(userRepository.findById(11L)).thenReturn(Optional.of(employeeUser));
        when(checklistItemRepository.existsByCompletedBy(employeeUser)).thenReturn(false);
        when(temperatureLogRepository.existsByLoggedBy(employeeUser)).thenReturn(false);
        when(deviationRepository.existsByReportedBy(employeeUser)).thenReturn(false);
        when(alcoholLogRepository.existsByRecordedBy(employeeUser)).thenReturn(true);

        ResourceConflictException ex = assertThrows(
                ResourceConflictException.class,
                () -> adminService.deleteUser(11L)
        );

        assertEquals(
                "User cannot be deleted because they have existing logs or deviations. Deactivate the user instead.",
                ex.getMessage()
        );

        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUser_shouldThrowWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> adminService.deleteUser(999L)
        );

        assertEquals("User not found: 999", ex.getMessage());
        verify(userRepository, never()).delete(any(User.class));
    }
}