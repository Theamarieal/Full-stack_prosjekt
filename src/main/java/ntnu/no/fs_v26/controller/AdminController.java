package ntnu.no.fs_v26.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin endpoints for user management")
public class AdminController {

  private final AdminService adminService;

  @Operation(summary = "Get all users", description = "Returns all users. Requires ADMIN role.")
  @ApiResponse(responseCode = "200", description = "List of users returned successfully")
  @ApiResponse(responseCode = "403", description = "Access denied")
  @GetMapping("/users")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
    return ResponseEntity.ok(adminService.getAllUsers());
  }

  @Operation(summary = "Create a new user", description = "Creates a user with email, password and role. Requires ADMIN role.")
  @ApiResponse(responseCode = "201", description = "User created successfully")
  @ApiResponse(responseCode = "400", description = "Invalid input or e-mail already in use")
  @ApiResponse(responseCode = "403", description = "Access denied")
  @PostMapping("/users")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, Object>> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createUser(request));
  }

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
}
