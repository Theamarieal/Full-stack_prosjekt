package ntnu.no.fs_v26.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) representing a simplified employee option.
 *
 * <p>This class is typically used to return a minimal representation of an employee,
 * for example in dropdowns, selections, or lists where only basic identifying
 * information is needed.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeOptionResponse {

    /**
     * The unique identifier of the employee.
     */
    private Long id;

    /**
     * The email address of the employee.
     */
    private String email;
}