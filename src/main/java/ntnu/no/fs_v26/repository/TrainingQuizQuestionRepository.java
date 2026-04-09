package ntnu.no.fs_v26.repository;

import java.util.List;
import ntnu.no.fs_v26.model.TrainingDocument;
import ntnu.no.fs_v26.model.TrainingQuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link TrainingQuizQuestion} entities.
 *
 * <p>Provides queries for retrieving quiz questions associated with
 * specific training documents.
 */
public interface TrainingQuizQuestionRepository extends JpaRepository<TrainingQuizQuestion, Long> {

    /**
     * Returns all quiz questions for a specific training document.
     *
     * @param document the training document whose questions to retrieve
     * @return a list of quiz questions for the given document
     */
    List<TrainingQuizQuestion> findByDocument(TrainingDocument document);
}