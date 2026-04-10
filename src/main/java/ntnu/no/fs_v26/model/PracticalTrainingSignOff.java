package ntnu.no.fs_v26.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

/**
 * Entity representing a practical training sign-off for a user.
 *
 * <p>A {@code PracticalTrainingSignOff} is used to record that a user has
 * completed a practical training requirement and has been approved by
 * another user (e.g. a supervisor or manager).</p>
 *
 * <p>The entity links a training document, the employee who completed the
 * training, and the user who approved it. It also stores approval status,
 * optional notes, and a timestamp for when the approval was granted.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "practical_training_signoffs")
public class PracticalTrainingSignOff {

    /**
     * Unique identifier for the sign-off record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The training document that the sign-off is associated with.
     */
    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private TrainingDocument document;

    /**
     * The employee who completed the practical training.
     */
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    /**
     * The user who approved the training (e.g. supervisor).
     */
    @ManyToOne
    @JoinColumn(name = "approved_by_id", nullable = false)
    private User approvedBy;

    /**
     * Optional note or comment related to the sign-off.
     */
    @Column(length = 1000)
    private String note;

    /**
     * Indicates whether the training was approved.
     */
    @Column(nullable = false)
    private boolean approved;

    /**
     * The date and time when the training was approved.
     */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * Lifecycle callback that sets the approval timestamp before persisting.
     *
     * <p>This ensures that {@code approvedAt} is automatically populated
     * when a sign-off record is created.</p>
     */
    @PrePersist
    protected void onCreate() {
        approvedAt = LocalDateTime.now();
    }
}