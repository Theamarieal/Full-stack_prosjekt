package ntnu.no.fs_v26.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    private String name;
    private double minTemp;
    private double maxTemp;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    @JsonIgnoreProperties({ "users" })
    private Organization organization;
}