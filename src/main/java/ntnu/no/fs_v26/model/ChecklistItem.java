package ntnu.no.fs_v26.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity representing a single item within a checklist.
 *
 * <p>A {@code ChecklistItem} represents an individual task or requirement
 * that belongs to a {@link Checklist}. Each item can be marked as completed,
 * along with information about who completed it and when.</p>
 *
 * <p>This entity is part of a parent-child relationship where multiple items
 * belong to one checklist.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "checklist_items")
public class ChecklistItem {

    /**
     * Unique identifier for the checklist item.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Description of the task or requirement.
     */
    private String description;

    /**
     * Indicates whether the item has been completed.
     */
    private boolean completed;

    /**
     * The date and time when the item was marked as completed.
     */
    private LocalDateTime completedAt;

    /**
     * The user who completed the checklist item.
     */
    @ManyToOne
    @JoinColumn(name = "completed_by")
    private User completedBy;

    /**
     * The checklist that this item belongs to.
     *
     * <p>This field is ignored during JSON serialization to prevent infinite
     * recursion when returning checklist data.</p>
     */
    @ManyToOne
    @JoinColumn(name = "checklist_id")
    @JsonIgnore
    private Checklist checklist;
}