package ntnu.no.fs_v26.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

/**
 * Entity representing a checklist within the system.
 *
 * <p>A {@code Checklist} defines a set of tasks or items that must be completed
 * within a specific module and frequency. It is associated with an organization
 * and consists of multiple {@link ChecklistItem}s.</p>
 *
 * <p>Checklists are typically used for operational routines, compliance checks,
 * or recurring tasks that need to be performed by users.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "checklists")
public class Checklist {

    /**
     * Unique identifier for the checklist.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The title of the checklist.
     */
    private String title;

    /**
     * A description providing additional context or instructions for the checklist.
     */
    private String description;

    /**
     * The frequency at which the checklist should be completed
     * (e.g. daily, weekly).
     */
    @Enumerated(EnumType.STRING)
    private Frequency frequency;

    /**
     * The module or category this checklist belongs to.
     */
    @Enumerated(EnumType.STRING)
    private ModuleType module;

    /**
     * The organization that owns the checklist.
     */
    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    /**
     * The list of items that make up this checklist.
     *
     * <p>Each {@link ChecklistItem} represents a single task or requirement
     * within the checklist.</p>
     */
    @OneToMany(mappedBy = "checklist", cascade = CascadeType.ALL)
    private List<ChecklistItem> items;
}