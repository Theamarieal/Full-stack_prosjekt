package ntnu.no.fs_v26.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body returned after successful registration or login.
 *
 * <p>Contains the JWT token to be used for subsequent authenticated requests,
 * along with basic user information such as email, role, and organization.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

  /** The JWT token to be included in the Authorization header for authenticated requests. */
  private String token;

  /**
   * Basic user information returned after authentication.
   * Includes email, role, and organization (id and name).
   */
  private Object user;
}