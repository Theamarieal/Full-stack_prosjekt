package ntnu.no.fs_v26.repository;

import java.util.List;
import ntnu.no.fs_v26.model.TrainingDocument;
import ntnu.no.fs_v26.model.TrainingQuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingQuizQuestionRepository extends JpaRepository<TrainingQuizQuestion, Long> {
    List<TrainingQuizQuestion> findByDocument(TrainingDocument document);
}