package ntnu.no.fs_v26.auth;

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

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        System.out.println("REGISTER REQUEST: email=" + request.getEmail()
                + ", role=" + request.getRole()
                + ", organizationId=" + request.getOrganizationId());
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
                .user(Map.of(
                        "email", savedUser.getEmail(),
                        "role", savedUser.getRole().name(),
                        "organization", Map.of(
                                "id", savedUser.getOrganization().getId(),
                                "name", savedUser.getOrganization().getName())))
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .user(Map.of(
                        "email", user.getEmail(),
                        "role", user.getRole().name(),
                        "organization", Map.of(
                                "id", user.getOrganization().getId(),
                                "name", user.getOrganization().getName())))
                .build();
    }
}