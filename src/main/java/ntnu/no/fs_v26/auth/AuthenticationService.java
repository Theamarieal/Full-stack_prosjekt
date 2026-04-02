package ntnu.no.fs_v26.auth;

import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.model.Organization;
import ntnu.no.fs_v26.repository.UserRepository;
import ntnu.no.fs_v26.repository.OrganizationRepository;
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
        // 1. Finn organisasjonen (vi antar ID 1 finnes)
        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new RuntimeException("Organisasjon ikke funnet"));

        // 2. Bygg bruker-objektet
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .organization(organization)
                .build();
        
        // 3. Lagre og generer token
        User savedUser = repository.save(user);
        String jwtToken = jwtService.generateToken(savedUser);
        
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .user(Map.of(
                    "email", savedUser.getEmail(), 
                    "role", savedUser.getRole().name()
                ))
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Bruker ikke funnet"));
        
        String jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
            .token(jwtToken)
            .user(Map.of("email", user.getEmail(), "role", user.getRole().name()))
            .build();
    }
}