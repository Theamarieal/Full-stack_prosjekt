package ntnu.no.fs_v26.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a certification issued to a user.
 *
 * <p>A {@code Certification} is created when a user successfully completes
 * a training document, such as a quiz or course. It links the user, the
 * training document, and the organization where the certification is valid.</p>
 *
 * <p>The entity also stores metadata such as the certification title and
 * the timestamp indicating when it was issued.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "certifications")
public class Certification {

    /**
     * Unique identifier for the certification.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The training document that this certification is based on.
     */
    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private TrainingDocument document;

    /**
     * The user who received the certification.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The organization in which the certification is valid.
     */
    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    /**
     * The title of the certification.
     */
    @Column(nullable = false)
    private String title;

    /**
     * The date and time when the certification was issued.
     */
    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    /**
     * Lifecycle callback that sets the issue timestamp before the entity is persisted.
     *
     * <p>This ensures that {@code issuedAt} is automatically populated when a
     * certification is created.</p>
     */
    @PrePersist
    protected void onCreate() {
        issuedAt = LocalDateTime.now();
    }
}