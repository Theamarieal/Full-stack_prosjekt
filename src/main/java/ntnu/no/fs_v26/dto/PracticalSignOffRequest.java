package ntnu.no.fs_v26.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) used to request a practical sign-off for an employee.
 *
 * <p>This class is typically used when confirming that an employee has completed
 * a practical task, assessment, or training.</p>
 */
@Data
public class PracticalSignOffRequest {

    /**
     * The ID of the employee being signed off.
     */
    private Long employeeId;

    /**
     * Optional note or comment related to the sign-off.
     */
    private String note;
}