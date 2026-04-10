package ntnu.no.fs_v26.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) used when creating a new quiz question.
 *
 * <p>This class represents the data required to define a multiple-choice
 * question, including the question text, four answer options, and the
 * correct answer.</p>
 */
@Data
public class CreateQuizQuestionRequest {

    /**
     * The text of the quiz question.
     */
    private String question;

    /**
     * The first answer option.
     */
    private String optionA;

    /**
     * The second answer option.
     */
    private String optionB;

    /**
     * The third answer option.
     */
    private String optionC;

    /**
     * The fourth answer option.
     */
    private String optionD;

    /**
     * The correct answer for the question.
     *
     * <p>Typically corresponds to one of the provided options (e.g. "A", "B", "C", or "D").</p>
     */
    private String correctAnswer;
}