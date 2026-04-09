package ntnu.no.fs_v26.service;

import lombok.RequiredArgsConstructor;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<Map<String, Object>> getAllUsers(User currentUser) {
        return userRepository.findByOrganization(currentUser.getOrganization()).stream()
            .map(user -> Map.<String, Object>of(
                "id", user.getId(),
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "active", user.isActive()))
            .toList();
    }

    /**
     * Creates a new user in the same organization as the authenticated admin.
     * The organization is always taken from the admin's JWT, never from the request body.
     *
     * @param request     the user details to create
     * @param currentUser the authenticated admin performing the action
     * @return a map with the created user's details
     */
    public Map<String, Object> createUser(AdminCreateUserRequest request, User currentUser) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResourceConflictException("E-mail already in use: " + request.getEmail());
        }

        User user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(request.getRole())
            .organization(currentUser.getOrganization())
            .active(true)
            .build();

        User saved = userRepository.save(user);

        return Map.of(
            "id", saved.getId(),
            "email", saved.getEmail(),
            "role", saved.getRole().name(),
            "active", saved.isActive());
    }

    /**
     * Creates a new organization and an admin user for it.
     *
     * @param request contains organization name, admin email and admin password
     * @return a map with the created organization and admin user details
     */
    @Transactional
    public Map<String, Object> createOrganization(AdminCreateOrganizationRequest request) {
        if (userRepository.findByEmail(request.getAdminEmail()).isPresent()) {
            throw new ResourceConflictException("E-mail already in use: " + request.getAdminEmail());
        }

        Organization organization = organizationRepository.save(
            Organization.builder()
                .name(request.getOrganizationName())
                .build());

        User admin = userRepository.save(User.builder()
            .email(request.getAdminEmail())
            .password(passwordEncoder.encode(request.getAdminPassword()))
            .role(Role.ADMIN)
            .organization(organization)
            .active(true)
            .build());

        return Map.of(
            "organizationId", organization.getId(),
            "organizationName", organization.getName(),
            "adminEmail", admin.getEmail(),
            "adminRole", admin.getRole().name());
    }

    public Map<String, Object> updateUserRole(Long userId, AdminUpdateRoleRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // Prevent downgrading the last admin in the organization
        if (user.getRole() == Role.ADMIN && request.getRole() != Role.ADMIN) {
            long adminCount = userRepository.findByOrganization(user.getOrganization())
                .stream()
                .filter(u -> u.getRole() == Role.ADMIN && u.isActive())
                .count();
            if (adminCount <= 1) {
                throw new ResourceConflictException(
                    "Cannot change role of the last admin in the organization.");
            }
        }

        user.setRole(request.getRole());
        User saved = userRepository.save(user);

        return Map.of(
            "id", saved.getId(),
            "email", saved.getEmail(),
            "role", saved.getRole().name(),
            "active", saved.isActive());
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
            "active", saved.isActive());
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        boolean hasReferences = checklistItemRepository.existsByCompletedBy(user)
            || temperatureLogRepository.existsByLoggedBy(user)
            || deviationRepository.existsByReportedBy(user)
            || alcoholLogRepository.existsByRecordedBy(user);

        if (hasReferences) {
            throw new ResourceConflictException(
                "User cannot be deleted because they have existing logs or deviations. Deactivate the user instead.");
        }

        userRepository.delete(user);
    }
}