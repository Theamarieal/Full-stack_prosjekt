package ntnu.no.fs_v26.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Entity representing a user in the system.
 *
 * <p>The {@code User} entity stores authentication and authorization data,
 * including credentials, role, and organization membership. It implements
 * {@link UserDetails} to integrate with Spring Security for authentication
 * and access control.</p>
 *
 * <p>Each user belongs to an organization and is assigned a {@link Role},
 * which determines access rights within the system.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

  /**
   * Unique identifier for the user.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * The user's email address, used as the username for authentication.
   */
  @Column(nullable = false, unique = true)
  private String email;

  /**
   * The user's hashed password.
   */
  @Column(nullable = false)
  private String password;

  /**
   * The role assigned to the user, used for authorization.
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  /**
   * Indicates whether the user account is active.
   */
  @Column(nullable = false)
  @Builder.Default
  private boolean active = true;

  /**
   * The organization the user belongs to.
   *
   * <p>This field is ignored during JSON serialization to prevent
   * circular references and unnecessary data exposure.</p>
   */
  @ManyToOne
  @JoinColumn(name = "organization_id", nullable = true)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private Organization organization;

  /**
   * The date and time when the user was created.
   */
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  /**
   * Lifecycle callback that sets the creation timestamp before persisting.
   */
  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  /**
   * Returns the authorities granted to the user.
   *
   * <p>The user's role is converted into a Spring Security authority
   * with the prefix {@code ROLE_}.</p>
   *
   * @return a collection containing the user's authorities
   */
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }

  /**
   * Returns the username used for authentication.
   *
   * <p>In this system, the email is used as the username.</p>
   *
   * @return the user's email
   */
  @Override
  public String getUsername() {
    return email;
  }

  /**
   * Indicates whether the user account has expired.
   *
   * @return {@code true}, as expiration is not currently enforced
   */
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  /**
   * Indicates whether the user account is locked.
   *
   * @return {@code true}, as locking is not currently enforced
   */
  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  /**
   * Indicates whether the user's credentials have expired.
   *
   * @return {@code true}, as credential expiration is not enforced
   */
  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  /**
   * Indicates whether the user account is enabled.
   *
   * <p>This reflects the {@code active} field.</p>
   *
   * @return {@code true} if the user is active, otherwise {@code false}
   */
  @Override
  public boolean isEnabled() {
    return active;
  }

  /**
   * Returns the user's password.
   *
   * @return the hashed password
   */
  @Override
  public String getPassword() {
    return password;
  }
}