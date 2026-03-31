package ntnu.no.fs_v26.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "equipment")
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // f.eks. "Fridge fish"
    private double minTemp; // f.eks. 0.0
    private double maxTemp; // f.eks. 4.0

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;
}