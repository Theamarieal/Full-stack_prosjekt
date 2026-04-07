package ntnu.no.fs_v26.service;

import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.controller.AdminCreateUserRequest;
import ntnu.no.fs_v26.controller.AdminUpdateRoleRequest;
import ntnu.no.fs_v26.model.Organization;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.repository.OrganizationRepository;
import ntnu.no.fs_v26.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

  private final UserRepository userRepository;
  private final OrganizationRepository organizationRepository;
  private final PasswordEncoder passwordEncoder;

  public List<Map<String, Object>> getAllUsers() {
    return userRepository.findAll().stream()
        .map(user -> Map.<String, Object>of(
            "id", user.getId(),
            "email", user.getEmail(),
            "role", user.getRole().name()
        ))
        .toList();
  }

  public Map<String, Object> createUser(AdminCreateUserRequest request) {
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new IllegalArgumentException("E-mail already in use: " + request.getEmail());
    }

    Organization organization = organizationRepository.findById(request.getOrganizationId())
        .orElseThrow(() -> new RuntimeException("Organization not found"));

    User user = User.builder()
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .role(request.getRole())
        .organization(organization)
        .build();

    User saved = userRepository.save(user);

    return Map.of(
        "id", saved.getId(),
        "email", saved.getEmail(),
        "role", saved.getRole().name()
    );
  }

  public Map<String, Object> updateUserRole(Long userId, AdminUpdateRoleRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found: " + userId));

    user.setRole(request.getRole());
    User saved = userRepository.save(user);

    return Map.of(
        "id", saved.getId(),
        "email", saved.getEmail(),
        "role", saved.getRole().name()
    );
  }
}
