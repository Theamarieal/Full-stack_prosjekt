package ntnu.no.fs_v26.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing equipment used within an organization.
 *
 * <p>An {@code Equipment} represents a physical unit, such as a fridge,
 * freezer, or other temperature-controlled device. It is associated with
 * an organization and includes temperature limits used for monitoring
 * and compliance purposes.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "equipment")
public class Equipment {

    /**
     * Unique identifier for the equipment.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name of the equipment.
     */
    private String name;

    /**
     * The minimum allowed temperature for the equipment.
     */
    private double minTemp;

    /**
     * The maximum allowed temperature for the equipment.
     */
    private double maxTemp;

    /**
     * The organization that owns the equipment.
     *
     * <p>The {@code users} field is ignored during JSON serialization to prevent
     * unnecessary data exposure and potential circular references.</p>
     */
    @ManyToOne
    @JoinColumn(name = "organization_id")
    @JsonIgnoreProperties({ "users" })
    private Organization organization;
}