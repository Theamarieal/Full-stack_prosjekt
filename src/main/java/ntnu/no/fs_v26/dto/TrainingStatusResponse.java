package ntnu.no.fs_v26.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Data Transfer Object (DTO) representing the training status of a document.
 *
 * <p>This class is used to return the current status of a training document
 * for a user, indicating whether it is completed, pending, or in progress.</p>
 */
@Data
@Builder
public class TrainingStatusResponse {

    /**
     * The ID of the training document.
     */
    private Long documentId;

    /**
     * The current status of the training (e.g. "COMPLETED", "PENDING", "IN_PROGRESS").
     */
    private String status;
}