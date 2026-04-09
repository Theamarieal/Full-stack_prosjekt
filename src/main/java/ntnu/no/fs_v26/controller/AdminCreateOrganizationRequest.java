package ntnu.no.fs_v26.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminCreateOrganizationRequest {

  @NotBlank(message = "Organization name is required")
  private String organizationName;

  @NotBlank(message = "Admin e-mail is required")
  @Email(message = "Must be a valid e-mail address")
  private String adminEmail;

  @NotBlank(message = "Admin password is required")
  @Size(min = 8, message = "Password must be at least 8 characters")
  private String adminPassword;
}
