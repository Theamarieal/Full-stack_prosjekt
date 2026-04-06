package ntnu.no.fs_v26.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "temperature_logs")
public class TemperatureLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reading_value")
    private double value;

    private LocalDateTime timestamp;
    private boolean isDeviation;

    @ManyToOne
    @JoinColumn(name = "equipment_id")
    @JsonIgnoreProperties({ "organization" })
    private Equipment equipment;

    @ManyToOne
    @JoinColumn(name = "logged_by")
    @JsonIgnoreProperties({ "organization", "password" })
    private User loggedBy;
}