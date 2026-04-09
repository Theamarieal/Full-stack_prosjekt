package ntnu.no.fs_v26.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for admin-only user and organization management.
 *
 * <p>All endpoints require the {@code ADMIN} role. Admins can only manage
 * users within their own organization, enforcing multi-tenancy.
 *
 * <p>Base path: {@code /api/v1/admin}
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin endpoints for user management")
public class AdminController {

  private final AdminService adminService;

  /**
   * Returns all users in the authenticated admin's organization.
   *
   * @param currentUser the authenticated admin user
   * @return a list of user objects for the organization
   */
  @Operation(summary = "Get all users", description = "Returns all users in the admin's organization. Requires ADMIN role.")
  @ApiResponse(responseCode = "200", description = "List of users returned successfully")
  @ApiResponse(responseCode = "403", description = "Access denied")
  @GetMapping("/users")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<Map<String, Object>>> getAllUsers(
      @AuthenticationPrincipal User currentUser) {
    return ResponseEntity.ok(adminService.getAllUsers(currentUser));
  }

  /**
   * Creates a new user in the authenticated admin's organization.
   *
   * @param request     the request body containing email, password, and role
   * @param currentUser the authenticated admin user
   * @return the created user object with HTTP 201
   */
  @Operation(summary = "Create a new user", description = "Creates a user in the admin's organization. Requires ADMIN role.")
  @ApiResponse(responseCode = "201", description = "User created successfully")
  @ApiResponse(responseCode = "400", description = "Invalid input or e-mail already in use")
  @ApiResponse(responseCode = "403", description = "Access denied")
  @PostMapping("/users")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, Object>> createUser(
      @Valid @RequestBody AdminCreateUserRequest request,
      @AuthenticationPrincipal User currentUser) {
    return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createUser(request, currentUser));
  }

  /**
   * Updates the role of an existing user.
   *
   * @param userId  the ID of the user to update
   * @param request the request body containing the new role
   * @return the updated user object
   */
  @Operation(summary = "Update user role", description = "Changes the role of a user. Requires ADMIN role.")
  @ApiResponse(responseCode = "200", description = "Role updated successfully")
  @ApiResponse(responseCode = "404", description = "User not found")
  @ApiResponse(responseCode = "403", description = "Access denied")
  @PatchMapping("/users/{userId}/role")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, Object>> updateUserRole(
      @PathVariable Long userId,
      @RequestBody AdminUpdateRoleRequest request) {
    return ResponseEntity.ok(adminService.updateUserRole(userId, request));
  }

  /**
   * Activates or deactivates a user account.
   *
   * @param userId  the ID of the user to update
   * @param request the request body containing the new active status
   * @return the updated user object
   */
  @Operation(summary = "Toggle user active status", description = "Activates or deactivates a user. Requires ADMIN role.")
  @ApiResponse(responseCode = "200", description = "Status updated successfully")
  @ApiResponse(responseCode = "404", description = "User not found")
  @ApiResponse(responseCode = "403", description = "Access denied")
  @PatchMapping("/users/{userId}/active")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, Object>> toggleUserActive(
      @PathVariable Long userId,
      @RequestBody AdminToggleActiveRequest request) {
    return ResponseEntity.ok(adminService.toggleUserActive(userId, request));
  }

  /**
   * Deletes a user by ID.
   *
   * @param userId the ID of the user to delete
   * @return HTTP 204 No Content on success
   */
  @Operation(summary = "Delete a user", description = "Deletes a user by ID. Requires ADMIN role.")
  @ApiResponse(responseCode = "204", description = "User deleted successfully")
  @ApiResponse(responseCode = "404", description = "User not found")
  @ApiResponse(responseCode = "403", description = "Access denied")
  @DeleteMapping("/users/{userId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
    adminService.deleteUser(userId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Creates a new organization along with an initial admin user.
   *
   * @param request the request body containing organization name and admin credentials
   * @return the created organization object with HTTP 201
   */
  @Operation(summary = "Create a new organization with an admin user",
      description = "Creates a new organization and an admin user for it. Requires ADMIN role.")
  @ApiResponse(responseCode = "201", description = "Organization created successfully")
  @ApiResponse(responseCode = "400", description = "Invalid input or e-mail already in use")
  @ApiResponse(responseCode = "403", description = "Access denied")
  @PostMapping("/organizations")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, Object>> createOrganization(
      @Valid @RequestBody AdminCreateOrganizationRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createOrganization(request));
  }
}
