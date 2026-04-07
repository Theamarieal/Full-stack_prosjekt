package ntnu.no.fs_v26.service;

import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.controller.AdminCreateUserRequest;
import ntnu.no.fs_v26.controller.AdminToggleActiveRequest;
import ntnu.no.fs_v26.controller.AdminUpdateRoleRequest;
import ntnu.no.fs_v26.exception.ResourceConflictException;
import ntnu.no.fs_v26.exception.ResourceNotFoundException;
import ntnu.no.fs_v26.model.Organization;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.repository.AlcoholLogRepository;
import ntnu.no.fs_v26.repository.ChecklistItemRepository;
import ntnu.no.fs_v26.repository.DeviationRepository;
import ntnu.no.fs_v26.repository.OrganizationRepository;
import ntnu.no.fs_v26.repository.TemperatureLogRepository;
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
  private final ChecklistItemRepository checklistItemRepository;
  private final TemperatureLogRepository temperatureLogRepository;
  private final DeviationRepository deviationRepository;
  private final AlcoholLogRepository alcoholLogRepository;

  public List<Map<String, Object>> getAllUsers() {
    return userRepository.findAll().stream()
        .map(user -> Map.<String, Object>of(
            "id", user.getId(),
            "email", user.getEmail(),
            "role", user.getRole().name(),
            "active", user.isActive()
        ))
        .toList();
  }

  public Map<String, Object> createUser(AdminCreateUserRequest request) {
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new IllegalArgumentException("E-mail already in use: " + request.getEmail());
    }

    Organization organization = organizationRepository.findById(request.getOrganizationId())
        .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

    User user = User.builder()
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .role(request.getRole())
        .organization(organization)
        .active(true)
        .build();

    User saved = userRepository.save(user);

    return Map.of(
        "id", saved.getId(),
        "email", saved.getEmail(),
        "role", saved.getRole().name(),
        "active", saved.isActive()
    );
  }

  public Map<String, Object> updateUserRole(Long userId, AdminUpdateRoleRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    user.setRole(request.getRole());
    User saved = userRepository.save(user);

    return Map.of(
        "id", saved.getId(),
        "email", saved.getEmail(),
        "role", saved.getRole().name(),
        "active", saved.isActive()
    );
  }

  public Map<String, Object> toggleUserActive(Long userId, AdminToggleActiveRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    user.setActive(request.isActive());
    User saved = userRepository.save(user);

    return Map.of(
        "id", saved.getId(),
        "email", saved.getEmail(),
        "role", saved.getRole().name(),
        "active", saved.isActive()
    );
  }

  public void deleteUser(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    boolean hasReferences =
        checklistItemRepository.existsByCompletedBy(user) ||
            temperatureLogRepository.existsByLoggedBy(user) ||
            deviationRepository.existsByReportedBy(user) ||
            alcoholLogRepository.existsByRecordedBy(user);

    if (hasReferences) {
      throw new ResourceConflictException(
          "User cannot be deleted because they have existing logs or deviations. Deactivate the user instead."
      );
    }

    userRepository.delete(user);
  }
}
