package ntnu.no.fs_v26.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "deviations")
public class Deviation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    
    @Enumerated(EnumType.STRING)
    private DeviationStatus status;

    @Enumerated(EnumType.STRING)
    private DeviationModule module;

    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    @ManyToOne
    @JoinColumn(name = "reported_by")
    private User reportedBy;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;
}