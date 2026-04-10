package ntnu.no.fs_v26.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity representing a deviation within the system.
 *
 * <p>A {@code Deviation} is used to record incidents, irregularities, or
 * non-compliant events that occur within an organization. It allows tracking
 * of issues from creation to resolution, including status and responsibility.</p>
 *
 * <p>Each deviation is associated with a reporting user and an organization,
 * and may belong to a specific module or domain area.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "deviations")
public class Deviation {

    /**
     * Unique identifier for the deviation.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Short title describing the deviation.
     */
    private String title;

    /**
     * Detailed description of the deviation.
     */
    private String description;
    
    /**
     * The current status of the deviation (e.g. OPEN, IN_PROGRESS, RESOLVED).
     */
    @Enumerated(EnumType.STRING)
    private DeviationStatus status;

    /**
     * The module or domain area where the deviation occurred.
     */
    @Enumerated(EnumType.STRING)
    private DeviationModule module;

    /**
     * The date and time when the deviation was created.
     */
    private LocalDateTime createdAt;

    /**
     * The date and time when the deviation was resolved.
     *
     * <p>This field is {@code null} until the deviation is resolved.</p>
     */
    private LocalDateTime resolvedAt;

    /**
     * The user who reported the deviation.
     */
    @ManyToOne
    @JoinColumn(name = "reported_by")
    private User reportedBy;

    /**
     * The organization where the deviation occurred.
     */
    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;
}