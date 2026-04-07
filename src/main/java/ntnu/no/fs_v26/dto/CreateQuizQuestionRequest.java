package ntnu.no.fs_v26.dto;

import lombok.Data;

@Data
public class CreateQuizQuestionRequest {
    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;
}