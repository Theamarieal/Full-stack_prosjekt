package ntnu.no.fs_v26.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EquipmentResponse {
    private Long id;
    private String name;
    private double minTemp;
    private double maxTemp;
}