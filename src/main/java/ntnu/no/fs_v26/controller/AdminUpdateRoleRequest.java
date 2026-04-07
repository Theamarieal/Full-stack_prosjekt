package ntnu.no.fs_v26.controller;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ntnu.no.fs_v26.model.Role;

@Data
public class AdminUpdateRoleRequest {

  @NotNull(message = "Role is required")
  private Role role;
}
