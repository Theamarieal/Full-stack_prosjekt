package ntnu.no.fs_v26.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) used to represent an acknowledgment request.
 *
 * <p>This class is typically used in API requests where a client needs to
 * confirm or acknowledge a specific action, event, or resource.</p>
 *
 * <p>The {@code acknowledged} field indicates whether the given item
 * has been acknowledged.</p>
 */
@Data
public class AcknowledgeRequest {

    /**
     * Indicates whether the entity has been acknowledged.
     *
     * <p>True if acknowledged, false otherwise.</p>
     */
    private boolean acknowledged;
}