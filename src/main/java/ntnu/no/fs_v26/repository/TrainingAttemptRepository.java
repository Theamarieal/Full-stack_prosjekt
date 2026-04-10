package ntnu.no.fs_v26.repository;

import java.util.List;
import java.util.Optional;
import ntnu.no.fs_v26.model.TrainingAttempt;
import ntnu.no.fs_v26.model.TrainingDocument;
import ntnu.no.fs_v26.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link TrainingAttempt} entities.
 *
 * <p>Tracks quiz attempts made by users on training documents,
 * including pass/fail results and completion timestamps.
 */
public interface TrainingAttemptRepository extends JpaRepository<TrainingAttempt, Long> {

    /**
     * Returns all training attempts made by a specific user.
     *
     * @param user the user whose attempts to retrieve
     * @return a list of training attempts for the given user
     */
    List<TrainingAttempt> findByUser(User user);

    /**
     * Returns the most recent training attempt by a specific user for a specific document.
     * Used to determine the user's latest quiz result and certification status.
     *
     * @param user     the user to look up
     * @param document the training document to look up
     * @return an {@link Optional} containing the most recent attempt, or empty if none exists
     */
    Optional<TrainingAttempt> findTopByUserAndDocumentOrderByCompletedAtDesc(User user, TrainingDocument document);
}