package ntnu.no.fs_v26.auth;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.model.Organization;
import ntnu.no.fs_v26.model.Role;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.repository.OrganizationRepository;
import ntnu.no.fs_v26.repository.UserRepository;
import ntnu.no.fs_v26.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service class handling user registration and authentication.
 *
 * <p>Responsible for creating new user accounts, validating credentials,
 * and generating JWT tokens upon successful registration or login.
 * Passwords are stored in hashed form using the configured {@link PasswordEncoder}.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user and returns a JWT token.
     *
     * <p>Looks up the organization by the provided ID, creates a new user with
     * the given credentials and role, and generates a JWT token for the saved user.
     * If no role is specified in the request, {@link Role#EMPLOYEE} is assigned by default.
     *
     * @param request the registration request containing email, password, role, and organization ID
     * @return an {@link AuthenticationResponse} containing the JWT token and basic user info
     * @throws RuntimeException if the specified organization does not exist
     */
    public AuthenticationResponse register(RegisterRequest request) {
        Organization organization = organizationRepository.findById(request.getOrganizationId())
            .orElseThrow(() -> new RuntimeException("Organization not found"));

        Role role = request.getRole() != null ? request.getRole() : Role.EMPLOYEE;

        User user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(role)
            .organization(organization)
            .build();

        User savedUser = repository.save(user);
        String jwtToken = jwtService.generateToken(savedUser);

        return AuthenticationResponse.builder()
            .token(jwtToken)
            .user(
                Map.of(
                    "email", savedUser.getEmail(),
                    "role", savedUser.getRole().name(),
                    "organization",
                    Map.of(
                        "id", savedUser.getOrganization().getId(),
                        "name", savedUser.getOrganization().getName())))
            .build();
    }

    /**
     * Authenticates an existing user and returns a JWT token.
     *
     * <p>Delegates credential verification to the Spring Security {@link AuthenticationManager}.
     * If authentication succeeds, a JWT token is generated for the user.
     *
     * @param request the login request containing email and password
     * @return an {@link AuthenticationResponse} containing the JWT token and basic user info
     * @throws RuntimeException if no user with the given email exists
     * @throws org.springframework.security.core.AuthenticationException if credentials are invalid
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = repository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

        String jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
            .token(jwtToken)
            .user(
                Map.of(
                    "email", user.getEmail(),
                    "role", user.getRole().name(),
                    "organization",
                    Map.of(
                        "id", user.getOrganization().getId(),
                        "name", user.getOrganization().getName())))
            .build();
    }
}