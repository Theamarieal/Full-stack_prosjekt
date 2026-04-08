package ntnu.no.fs_v26.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ntnu.no.fs_v26.model.Role;

@Data
public class AdminCreateUserRequest {

  @NotBlank(message = "E-mail is required")
  @Email(message = "Must be a valid e-mail address")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 8, message = "Password must be at least 8 characters")
  private String password;

  @NotNull(message = "Role is required")
  private Role role;
}
