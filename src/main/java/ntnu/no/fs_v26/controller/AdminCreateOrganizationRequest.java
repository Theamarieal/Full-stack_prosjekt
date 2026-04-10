package ntnu.no.fs_v26.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for creating a new organization with an initial admin user.
 */
@Data
public class AdminCreateOrganizationRequest {

  /** The name of the new organization. */
  @NotBlank(message = "Organization name is required")
  private String organizationName;

  /** The email address of the initial admin user for the new organization. */
  @NotBlank(message = "Admin e-mail is required")
  @Email(message = "Must be a valid e-mail address")
  private String adminEmail;

  /** The password for the initial admin user. Must be at least 8 characters. */
  @NotBlank(message = "Admin password is required")
  @Size(min = 8, message = "Password must be at least 8 characters")
  private String adminPassword;
}
