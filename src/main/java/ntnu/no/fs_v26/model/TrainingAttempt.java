package ntnu.no.fs_v26.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a user's attempt at completing a training quiz or assessment.
 *
 * <p>A {@code TrainingAttempt} stores the result of a user's interaction with a
 * {@link TrainingDocument}, including the achieved score, the total number of
 * questions, and whether the attempt was passed.</p>
 *
 * <p>This entity is useful for tracking training progress, storing historical
 * attempt data, and determining whether a user qualifies for certification.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "training_attempts")
public class TrainingAttempt {

    /**
     * Unique identifier for the training attempt.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The training document associated with this attempt.
     */
    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private TrainingDocument document;

    /**
     * The user who completed the training attempt.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The number of correct answers achieved in the attempt.
     */
    @Column(nullable = false)
    private int score;

    /**
     * The total number of questions included in the training.
     */
    @Column(nullable = false)
    private int totalQuestions;

    /**
     * Indicates whether the user passed the attempt.
     */
    @Column(nullable = false)
    private boolean passed;

    /**
     * The date and time when the training attempt was completed.
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Lifecycle callback that sets the completion timestamp before persisting.
     *
     * <p>This ensures that {@code completedAt} is automatically populated
     * when a new training attempt is stored.</p>
     */
    @PrePersist
    protected void onCreate() {
        completedAt = LocalDateTime.now();
    }
}