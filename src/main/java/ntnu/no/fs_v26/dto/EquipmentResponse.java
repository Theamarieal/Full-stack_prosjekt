package ntnu.no.fs_v26.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data Transfer Object (DTO) representing equipment details.
 *
 * <p>This class is used to return information about a piece of equipment,
 * including its identifier, name, and supported temperature range.</p>
 */
@Data
@AllArgsConstructor
public class EquipmentResponse {

    /**
     * The unique identifier of the equipment.
     */
    private Long id;

    /**
     * The name of the equipment.
     */
    private String name;

    /**
     * The minimum supported temperature for the equipment.
     */
    private double minTemp;

    /**
     * The maximum supported temperature for the equipment.
     */
    private double maxTemp;
}