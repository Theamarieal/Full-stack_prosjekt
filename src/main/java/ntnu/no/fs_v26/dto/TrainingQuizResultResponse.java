package ntnu.no.fs_v26.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrainingQuizResultResponse {
    private int score;
    private int totalQuestions;
    private boolean passed;
    private boolean certificationCreated;
    private String message;
}