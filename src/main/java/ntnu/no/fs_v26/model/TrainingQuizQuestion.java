package ntnu.no.fs_v26.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

/**
 * Entity representing a quiz question belonging to a training document.
 *
 * <p>A {@code TrainingQuizQuestion} stores a multiple-choice question
 * associated with a {@link TrainingDocument}. Each question contains
 * four answer alternatives and one correct answer.</p>
 *
 * <p>This entity is used when a training document is completed through
 * a quiz-based assessment.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "training_quiz_questions")
public class TrainingQuizQuestion {

    /**
     * Unique identifier for the quiz question.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The training document that this quiz question belongs to.
     */
    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private TrainingDocument document;

    /**
     * The text of the quiz question.
     */
    @Column(nullable = false, length = 1000)
    private String question;

    /**
     * The first answer option.
     */
    @Column(name = "option_a", nullable = false)
    private String optionA;

    /**
     * The second answer option.
     */
    @Column(name = "option_b", nullable = false)
    private String optionB;

    /**
     * The third answer option.
     */
    @Column(name = "option_c", nullable = false)
    private String optionC;

    /**
     * The fourth answer option.
     */
    @Column(name = "option_d", nullable = false)
    private String optionD;

    /**
     * The correct answer option.
     *
     * <p>This value is typically stored as a single letter such as
     * {@code "A"}, {@code "B"}, {@code "C"}, or {@code "D"}.</p>
     */
    @Column(name = "correct_answer", nullable = false, length = 1)
    private String correctAnswer;

    /**
     * The date and time when the quiz question was created.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Lifecycle callback that sets the creation timestamp before persisting.
     *
     * <p>This ensures that {@code createdAt} is automatically populated
     * when a new quiz question is created.</p>
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}