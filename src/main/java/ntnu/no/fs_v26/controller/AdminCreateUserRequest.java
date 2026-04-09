package ntnu.no.fs_v26.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ntnu.no.fs_v26.model.Role;

/**
 * Request body for creating a new user within an organization.
 * Used by admins to add employees or managers to their organization.
 */
@Data
public class AdminCreateUserRequest {

  /** The email address for the new user account. Must be a valid email format. */
  @NotBlank(message = "E-mail is required")
  @Email(message = "Must be a valid e-mail address")
  private String email;

  /** The password for the new user account. Must be at least 8 characters. */
  @NotBlank(message = "Password is required")
  @Size(min = 8, message = "Password must be at least 8 characters")
  private String password;

  /** The role to assign to the new user (e.g. EMPLOYEE, MANAGER, ADMIN). */
  @NotNull(message = "Role is required")
  private Role role;
}