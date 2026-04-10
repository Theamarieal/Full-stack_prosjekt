package ntnu.no.fs_v26.dto;

import java.util.Map;
import lombok.Data;

/**
 * Data Transfer Object (DTO) used when submitting answers to a quiz.
 *
 * <p>This class represents a mapping between question IDs and the selected
 * answer option for each question.</p>
 */
@Data
public class QuizSubmissionRequest {

    /**
     * A map containing the submitted answers.
     *
     * <p>The key represents the question ID, and the value represents the
     * selected answer option (typically "A", "B", "C", or "D").</p>
     */
    private Map<Long, String> answers;
}