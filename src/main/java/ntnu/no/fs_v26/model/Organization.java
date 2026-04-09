package ntnu.no.fs_v26.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing an organization within the system.
 *
 * <p>An {@code Organization} serves as the primary grouping unit for users,
 * resources, and data. Most domain entities, such as users, logs, checklists,
 * and deviations, are associated with an organization to ensure proper
 * data separation and multi-tenant support.</p>
 *
 * <p>The entity also stores metadata such as the organization name and
 * the timestamp of when it was created.</p>
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "organizations")
public class Organization {

  /**
   * Unique identifier for the organization.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * The name of the organization.
   */
  @Column(nullable = false)
  private String name;

  /**
   * The date and time when the organization was created.
   */
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  /**
   * Lifecycle callback that sets the creation timestamp before persisting.
   *
   * <p>This ensures that {@code createdAt} is automatically populated
   * when a new organization is created.</p>
   */
  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}