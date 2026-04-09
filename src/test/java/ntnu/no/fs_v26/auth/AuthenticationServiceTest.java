package ntnu.no.fs_v26.auth;

import ntnu.no.fs_v26.model.Organization;
import ntnu.no.fs_v26.model.Role;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.repository.OrganizationRepository;
import ntnu.no.fs_v26.repository.UserRepository;
import ntnu.no.fs_v26.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    private Organization organization;

    @BeforeEach
    void setUp() {
        organization = Organization.builder()
                .id(1L)
                .name("Everest Sushi")
                .build();
    }

    @Test
    void register_shouldSaveUserAndReturnToken_withRequestedRole() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("manager@test.no");
        request.setPassword("Secret123");
        request.setRole(Role.MANAGER);
        request.setOrganizationId(1L);

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(passwordEncoder.encode("Secret123")).thenReturn("encoded-password");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        when(repository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return User.builder()
                    .id(100L)
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .role(user.getRole())
                    .organization(user.getOrganization())
                    .build();
        });

        AuthenticationResponse response = authenticationService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());

        @SuppressWarnings("unchecked")
        Map<String, Object> userMap = (Map<String, Object>) response.getUser();
        assertEquals("manager@test.no", userMap.get("email"));
        assertEquals("MANAGER", userMap.get("role"));

        @SuppressWarnings("unchecked")
        Map<String, Object> orgMap = (Map<String, Object>) userMap.get("organization");
        assertEquals(1L, orgMap.get("id"));
        assertEquals("Everest Sushi", orgMap.get("name"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(captor.capture());

        User savedUser = captor.getValue();
        assertEquals("manager@test.no", savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals(Role.MANAGER, savedUser.getRole());
        assertEquals(organization, savedUser.getOrganization());

        verify(passwordEncoder).encode("Secret123");
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    void register_shouldDefaultRoleToEmployee_whenRoleIsNull() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("employee@test.no");
        request.setPassword("Secret123");
        request.setRole(null);
        request.setOrganizationId(1L);

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(passwordEncoder.encode("Secret123")).thenReturn("encoded-password");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        when(repository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return User.builder()
                    .id(101L)
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .role(user.getRole())
                    .organization(user.getOrganization())
                    .build();
        });

        AuthenticationResponse response = authenticationService.register(request);

        assertNotNull(response);

        @SuppressWarnings("unchecked")
        Map<String, Object> userMap = (Map<String, Object>) response.getUser();
        assertEquals("EMPLOYEE", userMap.get("role"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(captor.capture());
        assertEquals(Role.EMPLOYEE, captor.getValue().getRole());
    }

    @Test
    void register_shouldThrow_whenOrganizationNotFound() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("employee@test.no");
        request.setPassword("Secret123");
        request.setRole(Role.EMPLOYEE);
        request.setOrganizationId(999L);

        when(organizationRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authenticationService.register(request)
        );

        assertEquals("Organization not found", ex.getMessage());

        verify(repository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void authenticate_shouldAuthenticateAndReturnToken() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("employee@test.no");
        request.setPassword("Secret123");

        User user = User.builder()
                .id(200L)
                .email("employee@test.no")
                .password("encoded-password")
                .role(Role.EMPLOYEE)
                .organization(organization)
                .build();

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(repository.findByEmail("employee@test.no")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthenticationResponse response = authenticationService.authenticate(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());

        @SuppressWarnings("unchecked")
        Map<String, Object> userMap = (Map<String, Object>) response.getUser();
        assertEquals("employee@test.no", userMap.get("email"));
        assertEquals("EMPLOYEE", userMap.get("role"));

        @SuppressWarnings("unchecked")
        Map<String, Object> orgMap = (Map<String, Object>) userMap.get("organization");
        assertEquals(1L, orgMap.get("id"));
        assertEquals("Everest Sushi", orgMap.get("name"));

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());

        UsernamePasswordAuthenticationToken authToken = captor.getValue();
        assertEquals("employee@test.no", authToken.getPrincipal());
        assertEquals("Secret123", authToken.getCredentials());

        verify(repository).findByEmail("employee@test.no");
        verify(jwtService).generateToken(user);
    }

    @Test
    void authenticate_shouldThrow_whenUserNotFoundAfterAuthentication() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("missing@test.no");
        request.setPassword("Secret123");

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(repository.findByEmail("missing@test.no")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authenticationService.authenticate(request)
        );

        assertEquals("User not found", ex.getMessage());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any(User.class));
    }
}