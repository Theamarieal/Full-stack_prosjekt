package ntnu.no.fs_v26.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

/**
 * Entity representing a user's acknowledgement of a training document.
 *
 * <p>A {@code DocumentAcknowledgement} records whether a user has acknowledged
 * (read and confirmed) a specific {@link TrainingDocument}. This is typically
 * used for compliance purposes to ensure that required documents have been
 * reviewed by users.</p>
 *
 * <p>The entity links a user to a document and stores both the acknowledgement
 * status and the timestamp of when the acknowledgement occurred.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "document_acknowledgements")
public class DocumentAcknowledgement {

    /**
     * Unique identifier for the acknowledgement record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The training document that is being acknowledged.
     */
    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private TrainingDocument document;

    /**
     * The user who acknowledges the document.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Indicates whether the document has been acknowledged.
     */
    @Column(nullable = false)
    private boolean acknowledged;

    /**
     * The date and time when the document was acknowledged.
     */
    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    /**
     * Lifecycle callback that sets the acknowledgement timestamp before persisting.
     *
     * <p>This ensures that {@code acknowledgedAt} is automatically populated
     * when a new acknowledgement is created.</p>
     */
    @PrePersist
    protected void onCreate() {
        acknowledgedAt = LocalDateTime.now();
    }
}