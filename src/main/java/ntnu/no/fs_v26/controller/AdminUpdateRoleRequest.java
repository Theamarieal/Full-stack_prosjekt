package ntnu.no.fs_v26.controller;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ntnu.no.fs_v26.model.Role;

/**
 * Request body for updating the role of an existing user.
 * Used by admins to promote or demote users within their organization.
 */
@Data
public class AdminUpdateRoleRequest {

  /** The new role to assign to the user (e.g. EMPLOYEE, MANAGER, ADMIN). */
  @NotNull(message = "Role is required")
  private Role role;
}