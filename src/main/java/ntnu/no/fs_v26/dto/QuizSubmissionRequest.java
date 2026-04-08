package ntnu.no.fs_v26.dto;

import java.util.Map;
import lombok.Data;

@Data
public class QuizSubmissionRequest {
    private Map<Long, String> answers; // questionId -> A/B/C/D
}