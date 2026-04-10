package ntnu.no.fs_v26.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ntnu.no.fs_v26.model.Role;

/**
 * Request body for the user registration endpoint.
 *
 * <p>Contains all required fields to create a new user account.
 * All fields are validated before the request is processed.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    /** The email address for the new account. Must be a valid email format and not exceed 255 characters. */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    /** The password for the new account. Must be between 8 and 255 characters. */
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    private String password;

    /** The role to assign to the new user (e.g. EMPLOYEE, MANAGER, ADMIN). */
    @NotNull(message = "Role is required")
    private Role role;

    /** The ID of the organization the new user belongs to. */
    @NotNull(message = "Organization ID is required")
    private Long organizationId;
}
