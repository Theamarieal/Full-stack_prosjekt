package ntnu.no.fs_v26.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Data Transfer Object (DTO) representing the result of a completed training quiz.
 *
 * <p>This class contains information about the user's performance, including
 * score, pass status, and whether a certification was generated.</p>
 */
@Data
@Builder
public class TrainingQuizResultResponse {

    /**
     * The number of correctly answered questions.
     */
    private int score;

    /**
     * The total number of questions in the quiz.
     */
    private int totalQuestions;

    /**
     * Indicates whether the user passed the quiz.
     */
    private boolean passed;

    /**
     * Indicates whether a certification was created as a result of passing.
     */
    private boolean certificationCreated;

    /**
     * A message providing additional feedback or information about the result.
     */
    private String message;
}