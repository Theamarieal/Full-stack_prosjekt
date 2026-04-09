package ntnu.no.fs_v26.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

/**
 * Entity representing a training document within the system.
 *
 * <p>A {@code TrainingDocument} defines training material that users must complete.
 * This may include written content, quizzes, or practical training requirements.</p>
 *
 * <p>The document is associated with an organization and can be configured with
 * different completion types, determining how users must complete the training
 * (e.g. read and acknowledge, pass a quiz, or receive a practical sign-off).</p>
 *
 * <p>The entity also supports file-based content and metadata such as creation time
 * and activation status.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "training_documents")
public class TrainingDocument {

    /**
     * Unique identifier for the training document.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The title of the training document.
     */
    @Column(nullable = false)
    private String title;

    /**
     * A short description of the training document.
     */
    @Column(length = 2000)
    private String description;

    /**
     * The main content of the training document.
     *
     * <p>Stored as large text, allowing detailed instructional material.</p>
     */
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * The type of training document (e.g. policy, guideline, quiz).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrainingDocumentType type;

    /**
     * Defines how the training must be completed.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "completion_type", nullable = false)
    private TrainingCompletionType completionType;

    /**
     * The organization that owns the training document.
     */
    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    /**
     * Indicates whether the document is active and available to users.
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    /**
     * The name of an associated file (if applicable).
     */
    @Column(name = "file_name")
    private String fileName;

    /**
     * The storage path of the associated file (if applicable).
     */
    @Column(name = "file_path")
    private String filePath;

    /**
     * The date and time when the document was created.
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
}