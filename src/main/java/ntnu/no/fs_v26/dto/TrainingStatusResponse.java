package ntnu.no.fs_v26.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrainingStatusResponse {
    private Long documentId;
    private String status;
}