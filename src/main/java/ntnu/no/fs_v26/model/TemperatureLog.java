package ntnu.no.fs_v26.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity representing a temperature measurement log.
 *
 * <p>A {@code TemperatureLog} records a temperature reading from a specific
 * piece of {@link Equipment}. It is used for monitoring and compliance purposes,
 * ensuring that temperature-sensitive equipment operates within allowed ranges.</p>
 *
 * <p>Each log entry includes the recorded value, timestamp, and whether the
 * reading is considered a deviation from acceptable limits.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "temperature_logs")
public class TemperatureLog {

    /**
     * Unique identifier for the temperature log.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The recorded temperature value.
     */
    @Column(name = "reading_value")
    private double value;

    /**
     * The date and time when the temperature was recorded.
     */
    private LocalDateTime timestamp;

    /**
     * Indicates whether the recorded value is outside acceptable limits.
     */
    private boolean isDeviation;

    /**
     * The equipment from which the temperature was recorded.
     *
     * <p>The organization field is ignored during JSON serialization to
     * prevent unnecessary data exposure and circular references.</p>
     */
    @ManyToOne
    @JoinColumn(name = "equipment_id")
    @JsonIgnoreProperties({ "organization" })
    private Equipment equipment;

    /**
     * The user who recorded the temperature.
     *
     * <p>Sensitive fields such as password and organization are excluded
     * during serialization.</p>
     */
    @ManyToOne
    @JoinColumn(name = "logged_by")
    @JsonIgnoreProperties({ "organization", "password" })
    private User loggedBy;
}